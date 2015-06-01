package org.veripacks.reader.dependencies

import org.objectweb.asm._
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import org.veripacks._

/**
 * Based on the DependencyVisitor example from the ASM distribution.
 *
 * Original copyright notice:
 * ASM examples: examples showing how ASM can be used
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Eugene Kuleshov and Adam Warski
 */
class DependencyVisitor(sourceFileName: String) extends ClassVisitor(Opcodes.ASM5) {
  val usages = new collection.mutable.HashMap[ClassName, ClassUsageDetail]

  private var currentUsageDetail: ClassUsageDetail = _

  override def visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array[String]) {
    currentUsageDetail = ClassSignatureUsageDetail(sourceFileName)

    if (signature == null) {
      if (superName != null) {
        addInternalName(superName)
      }
      addInternalNames(interfaces)
    }
    else {
      addSignature(signature)
    }
  }

  override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor = {
    addDesc(desc)
    new AnnotationDependencyVisitor
  }

  override def visitField(access: Int, name: String, desc: String, signature: String, value: AnyRef): FieldVisitor = {
    currentUsageDetail = FieldUsageDetail(sourceFileName, name)

    if (signature == null) {
      addDesc(desc)
    }
    else {
      addTypeSignature(signature)
    }
    if (value.isInstanceOf[Type]) {
      addType(value.asInstanceOf[Type])
    }
    new FieldDependencyVisitor
  }

  override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]): MethodVisitor = {
    currentUsageDetail = MethodSignatureUsageDetail(sourceFileName, name)

    if (signature == null) {
      addMethodDesc(desc)
    }
    else {
      addSignature(signature)
    }
    addInternalNames(exceptions)
    new MethodDependencyVisitor(name)
  }

  private def addName(name: String) {
    if (name == null) {
      return
    }

    val dottedName = name.replace('/', '.')
    val className = ClassName.fromDottedName(dottedName)

    usages(className) = usages.get(className) match {
      case None => currentUsageDetail
      case Some(MultipleUsageDetail(otherUsages)) => MultipleUsageDetail(otherUsages + currentUsageDetail)
      case Some(otherUsage) => MultipleUsageDetail(Set(otherUsage, currentUsageDetail))
    }
  }

  private def addInternalName(name: String) {
    addType(Type.getObjectType(name))
  }

  private def addInternalNames(names: Array[String]) {
    if (names != null) {
      names.foreach(addInternalName(_))
    }
  }

  private def addDesc(desc: String) {
    addType(Type.getType(desc))
  }

  private def addMethodDesc(desc: String) {
    addType(Type.getReturnType(desc))
    val types = Type.getArgumentTypes(desc)
    types.foreach(addType(_))
  }

  private def addType(t: Type) {
    t.getSort match {
      case Type.ARRAY   => addType(t.getElementType)
      case Type.OBJECT  => addName(t.getInternalName)
      case Type.METHOD  => addMethodDesc(t.getDescriptor)
      case _            => // ignore
    }
  }

  private def addSignature(signature: String) {
    if (signature != null) {
      new SignatureReader(signature).accept(new SignatureDependencyVisitor)
    }
  }

  private def addTypeSignature(signature: String) {
    if (signature != null) {
      new SignatureReader(signature).acceptType(new SignatureDependencyVisitor)
    }
  }

  private def addConstant(cst: AnyRef) {
    cst match {
      case t: Type => {
        addType(t)
      }
      case h: Handle => {
        addInternalName(h.getOwner)
        addMethodDesc(h.getDesc)
      }
      case _ => // Ignore
    }
  }

  private class AnnotationDependencyVisitor extends AnnotationVisitor(Opcodes.ASM5) {
    override def visit(name: String, value: AnyRef) {
      if (value.isInstanceOf[Type]) {
        addType(value.asInstanceOf[Type])
      }
    }

    override def visitEnum(name: String, desc: String, value: String) {
      addDesc(desc)
    }

    override def visitAnnotation(name: String, desc: String) = {
      addDesc(desc)
      this
    }

    override def visitArray(name: String) = {
      this
    }
  }

  private class FieldDependencyVisitor extends FieldVisitor(Opcodes.ASM5) {
    override def visitAnnotation(desc: String, visible: Boolean) = {
      addDesc(desc)
      new AnnotationDependencyVisitor
    }
  }

  private class MethodDependencyVisitor(methodName: String) extends MethodVisitor(Opcodes.ASM5) {
    override def visitAnnotationDefault = {
      new AnnotationDependencyVisitor
    }

    override def visitAnnotation(desc: String, visible: Boolean) = {
      addDesc(desc)
      new AnnotationDependencyVisitor
    }

    override def visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean) = {
      addDesc(desc)
      new AnnotationDependencyVisitor
    }

    override def visitTypeInsn(opcode: Int, `type`: String) {
      addType(Type.getObjectType(`type`))
    }

    override def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
      addInternalName(owner)
      addDesc(desc)
    }

    override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) = {
      addInternalName(owner)
      addMethodDesc(desc)
    }

    override def visitInvokeDynamicInsn(name: String, desc: String, bsm: Handle, bsmArgs: AnyRef*) {
      addMethodDesc(desc)
      addConstant(bsm)
      bsmArgs.foreach(addConstant)
    }

    override def visitLdcInsn(cst: AnyRef) {
      addConstant(cst)
    }

    override def visitMultiANewArrayInsn(desc: String, dims: Int) {
      addDesc(desc)
    }

    override def visitLocalVariable(name: String, desc: String, signature: String, start: Label, end: Label, index: Int) {
      addTypeSignature(signature)
    }

    override def visitTryCatchBlock(start: Label, end: Label, handler: Label, `type`: String) {
      if (`type` != null) {
        addInternalName(`type`)
      }
    }

    override def visitLineNumber(line: Int, start: Label) {
      currentUsageDetail = MethodBodyUsageDetail(sourceFileName, methodName, line)
    }
  }

  private class SignatureDependencyVisitor extends SignatureVisitor(Opcodes.ASM5) {
    override def visitClassType(name: String) {
      signatureClassName = name
      addInternalName(name)
    }

    override def visitInnerClassType(name: String) {
      signatureClassName = signatureClassName + "$" + name
      addInternalName(signatureClassName)
    }

    private var signatureClassName: String = _
  }

}