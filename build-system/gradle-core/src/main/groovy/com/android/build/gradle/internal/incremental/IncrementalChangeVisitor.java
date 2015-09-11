/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.build.gradle.internal.incremental;

import com.android.annotations.NonNull;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.List;

/**
 * Visitor for classes that have been changed since the initial push.
 *
 * This will generate a new class which name is the original class name + Support. This class will
 * have a static method for each method found in the updated class.
 *
 * The static method will be invoked from the generated access$dispatch method
 * following a delegation request issued by the original method implementation (through the bytecode
 * injection done in {@link IncrementalSupportVisitor}.
 *
 * So far the static method implementation do not require any change since the "this" parameter
 * is passed as the first parameter and is available in register 0.
 */
public class IncrementalChangeVisitor extends IncrementalVisitor {

    // todo : find a better way to specify logging and append to a log file.
    private static final boolean DEBUG = false;


    public IncrementalChangeVisitor(ClassNode classNode, List<ClassNode> parentNodes, ClassVisitor classVisitor) {
        super(classNode, parentNodes, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces) {
        super.visit(version, access, name + "$override", signature, "java/lang/Object",
                new String[]{CHANGE_TYPE.getInternalName()});

        if (DEBUG) {
            System.out.println(">>>>>>>> Processing " + name + "<<<<<<<<<<<<<");
        }

        visitedClassName = name;
        visitedSuperName = superName;

        // Create empty constructor
        MethodVisitor mv = super
                .visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V",
                false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions) {

        if (name.equals("<clinit>")) {
            return null;
        }


        boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
        String newDesc = isStatic ? desc : "(L" + visitedClassName + ";" + desc.substring(1);

        if (DEBUG) {
            System.out.println(">>> Visiting method " + visitedClassName + ":" + name + ":" + desc);
            if (exceptions != null) {
                for (String exception : exceptions) {
                    System.out.println("> Exception thrown : " + exception);
                }
            }
        }
        if (DEBUG) {
            System.out.println("New Desc is " + newDesc + ":" + isStatic);
        }

        String newName = getOverridenName(name);
        // Do not carry on any access flags from the original method. For example synchronized
        // on the original method would translate into a static synchronized method here.
        access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
        MethodVisitor original = super.visitMethod(access, newName, newDesc, signature, exceptions);
        if (name.equals("<init>")) {
            return new ConstructorVisitor(Opcodes.ASM5, original, access, newName, newDesc);
        } else {
            return new ISVisitor(Opcodes.ASM5, original, access, newName, newDesc, isStatic);
        }
    }

    /**
     * {@link MethodVisitor} implementation that is effectively swallowing all code until
     * {@link #stopIgnoring()} method is called.
     */
    public static class IgnoringMethodVisitorAdapter extends MethodVisitor {

        private MethodVisitor delegateVisitor;

        public IgnoringMethodVisitorAdapter(int api, MethodVisitor mv) {
            super(api, null);
            delegateVisitor = mv;
        }

        public void stopIgnoring() {
            super.mv = delegateVisitor;
        }
    }

    public class ConstructorVisitor extends ISVisitor {

        String desc;
        IgnoringMethodVisitorAdapter adapter;

        public ConstructorVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
            super(api, new IgnoringMethodVisitorAdapter(api, mv), access, name, desc, false);
            adapter = (IgnoringMethodVisitorAdapter) this.mv;
            this.desc = desc;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc,
                boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")
                    && owner.equals(visitedSuperName)) {
                adapter.stopIgnoring();
            }
        }
    }


    public class ISVisitor extends GeneratorAdapter {

        private final boolean isStatic;

        public ISVisitor(
                int api,
                MethodVisitor mv,
                int access,
                String name,
                String desc,
                boolean isStatic) {
            super(api, mv, access, name, desc);
            this.isStatic = isStatic;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (DEBUG) {
                System.out.println(
                        "Visit field access : " + owner + ":" + name + ":" + desc + ":" + isStatic);
            }
            // if we are access another object's field, nothing needs to be done.
            if (!owner.equals(visitedClassName)) {
                if (DEBUG) {
                    System.out.println("Not ours, unchanged field access");
                }
                // this is probably incorrect, what about if we access a package private field
                // of some other object, we need to go through reflection.
                super.visitFieldInsn(opcode, owner, name, desc);
                return;
            }

            // check the field access bits.
            FieldNode fieldNode = getFieldByName(name);
            if (fieldNode == null) {
                // this is an error, we should know of the fields we are visiting.
                throw new RuntimeException("Unknown field access " + name);
            }
            boolean isPrivate = (fieldNode.access & Opcodes.ACC_PRIVATE) != 0;
            boolean isProtected = (fieldNode.access & Opcodes.ACC_PROTECTED) != 0;
            boolean isPackagePrivate = fieldNode.access == 0;
            boolean isAccessedFieldStatic = (fieldNode.access & Opcodes.ACC_STATIC) != 0;

            // we should make this more efficient, have a per field access type method
            // for getting and setting field values.
            if (isPrivate || isProtected || isPackagePrivate) {
                if (isAccessedFieldStatic) {
                    // if we are dealing with accessing a static field, there is no "this" or
                    // object reference on the stack, push null for the first parameter value.
                    // eventually, this will fail at runtime since we use the object reference
                    // to look up the field which won't work with null, but it satisfies the
                    // ASM generator for now. We probably should do getClass() and pass that
                    // to a new method.
                    if (DEBUG) {
                        System.out.println("Dealing with a static field");
                    }
                    visitInsn(Opcodes.ACONST_NULL);
                }
                if (opcode == Opcodes.GETFIELD) {
                    if (DEBUG) {
                        System.out.println("Get field");
                    }
                    push(name);
                    invokeStatic(RUNTIME_TYPE,
                            Method.getMethod("Object getPrivateField(Object, String)"));
                    unbox(Type.getType(desc));
                }
                if (opcode == Opcodes.PUTFIELD) {
                    if (DEBUG) {
                        System.out.println("Set field");
                    }
                    box(Type.getType(desc));
                    push(name);
                    invokeStatic(RUNTIME_TYPE,
                            Method.getMethod(
                                    "void setPrivateField(Object, Object, String)"));
                }
            } else {
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc,
                boolean itf) {

            if (DEBUG) {
                System.out.println("Generic Method dispatch : " + opcode +
                        ":" + owner + ":" + name + ":" + desc + ":" + itf + ":" + isStatic);
            }
            boolean opcodeHandled = false;
            if (opcode == Opcodes.INVOKESPECIAL) {
                opcodeHandled = handleSpecialOpcode(opcode, owner, name, desc, itf);
            } else if (opcode == Opcodes.INVOKEVIRTUAL) {
                opcodeHandled = handleVirtualOpcode(opcode, owner, name, desc, itf);
            } else if (opcode == Opcodes.INVOKESTATIC) {
                opcodeHandled = handleStaticOpcode(opcode, owner, name, desc, itf);
            }
            if (DEBUG) {
                System.out.println("Opcode handled ? " + opcodeHandled);
            }
            if (!opcodeHandled) {
                mv.visitMethodInsn(opcode, owner, name, desc, itf);
            }
            if (DEBUG) {
                System.out.println("Done with generic method dispatch");
            }
        }

        private boolean handleSpecialOpcode(int opcode, String owner, String name, String desc,
                boolean itf) {
            if (owner.equals(visitedSuperName)) {
                if (DEBUG) {
                    System.out.println(
                            "Super Method dispatch : " + name + ":" + desc + ":" + itf + ":"
                                    + isStatic);
                }
                int arr = boxParametersToNewLocalArray(Type.getArgumentTypes(desc));
                push(name + "." + desc);
                loadLocal(arr);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, visitedClassName, "access$super",
                        "(L" + visitedClassName
                                + ";Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
                        false);
                Type ret = Type.getReturnType(desc);
                if (ret.getSort() == Type.VOID) {
                    pop();
                } else {
                    unbox(ret);
                }
                return true;
            } else if (owner.equals(visitedClassName)) {
                if (DEBUG) {
                    System.out.println(
                            "Private Method dispatch : " + name + ":" + desc + ":" + itf + ":"
                                    + isStatic);
                }
                // private method dispatch, just invoke the $override class static method.
                String newDesc = "(L" + visitedClassName + ";" + desc.substring(1);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, owner + "$override", name, newDesc, itf);
                return true;
            }
            return false;
        }

        private boolean handleVirtualOpcode(int opcode, String owner, String name, String desc,
                boolean itf) {
            if (owner.equals(visitedClassName)) {

                if (DEBUG) {
                    System.out.println(
                            "Method dispatch : " + name + ":" + desc + ":" + itf + ":" + isStatic);
                }
                MethodNode methodNode = getMethodByName(name, desc);
                boolean isPublic = methodNode != null
                        && ((methodNode.access & Opcodes.ACC_PUBLIC) != 0);

                // if this is a public method, just let the normal invoke virtual invoke the
                // original method implementation which in most case will just call back
                // into the enhanced code.
                if (isPublic) {
                    return false;
                }

                // for anything else, private, protected and package private, we must go through
                // reflection.
                Type[] parameterTypes = Type.getArgumentTypes(desc);

                int parameters = boxParametersToNewLocalArray(parameterTypes);

                push(name);
                push(parameterTypes.length);
                newArray(Type.getType(String.class));

                for (int i = 0; i < parameterTypes.length; i++) {
                    dup();
                    push(i);
                    push(parameterTypes[i].getClassName());
                    arrayStore(Type.getType(String.class));
                }

                loadLocal(parameters);

                invokeStatic(RUNTIME_TYPE,
                        Method.getMethod(
                                "Object invokeProtectedMethod(Object, String, String[], Object[])"));
                Type ret = Type.getReturnType(desc);
                if (ret.getSort() == Type.VOID) {
                    pop();
                } else {
                    unbox(ret);
                }
                return true;
            }
            return false;
        }

        private int boxParametersToNewLocalArray(Type[] parameterTypes) {
            int parameters = newLocal(Type.getType("[Ljava/lang.Object;"));
            push(parameterTypes.length);
            newArray(Type.getType(Object.class));
            storeLocal(parameters);

            for (int i = parameterTypes.length - 1; i >= 0; i--) {
                loadLocal(parameters);
                swap(parameterTypes[i], Type.getType(Object.class));
                push(i);
                swap(parameterTypes[i], Type.INT_TYPE);
                box(parameterTypes[i]);
                arrayStore(Type.getType(Object.class));
            }
            return parameters;
        }

        // we must do something similar as for non static method,
        // which is to call back the ORIGINAL static method
        // using reflection when the called method is anything but public.
        // I thought for a while we could bypass and call directly the $override method
        // but we can't because the static method could be of the super class and we don't
        // necessarily have an enhanced class to call directly.
        private boolean handleStaticOpcode(int opcode, String owner, String name, String desc,
                boolean itf) {
            return false;
        }

        @Override
        public void visitEnd() {
            if (DEBUG) {
                System.out.println("Method visit end");
            }
        }
    }

    @Override
    public void visitEnd() {
        int access = Opcodes.ACC_PUBLIC | Opcodes.ACC_VARARGS;
        Method m = new Method("access$dispatch", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
        MethodVisitor visitor = super.visitMethod(access,
                m.getName(),
                m.getDescriptor(),
                null, null);

        GeneratorAdapter mv = new GeneratorAdapter(access, m, visitor);

        if (TRACING_ENABLED) {
            mv.push("Redirecting ");
            mv.loadArg(0);
            trace(mv, 2);
        }

        List<MethodNode> methods = classNode.methods;
        for (MethodNode methodNode : methods) {
            if (methodNode.name.equals("<clinit>")) {
                continue;
            }
            String name = methodNode.name;
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitLdcInsn(name + "." + methodNode.desc);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
                    "(Ljava/lang/Object;)Z", false);
            Label l0 = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, l0);
            // this should be abstracted somewhere.
            String newDesc = (methodNode.access & Opcodes.ACC_STATIC) != 0
                    ? methodNode.desc
                    : "(L" + visitedClassName + ";" + methodNode.desc.substring(1);

            if (TRACING_ENABLED) {
                trace(mv, "M: " + name + " P:" + newDesc);
            }
            Type[] args = Type.getArgumentTypes(newDesc);
            int argc = 0;
            for (Type t : args) {
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.push(argc);
                mv.visitInsn(Opcodes.AALOAD);
                mv.unbox(t);
                argc++;
            }
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, visitedClassName + "$override", getOverridenName(name), newDesc,
                    false);
            Type ret = Type.getReturnType(methodNode.desc);
            if (ret.getSort() == Type.VOID) {
                mv.visitInsn(Opcodes.ACONST_NULL);
            } else {
                mv.box(ret);
            }
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(l0);
        }
        // this is an exception, we cannot find the method to dispatch, the verifier should have
        // flagged this and refused the hotswaping, generate an exception.
        // we could not find the method to invoke, prepare an exception to be thrown.
        mv.newInstance(Type.getType(StringBuilder.class));
        mv.dup();
        mv.invokeConstructor(Type.getType(StringBuilder.class), Method.getMethod("void <init>()V"));

        // TODO: have a common exception generation function.
        // create a meaningful message
        mv.push("Method not found ");
        mv.invokeVirtual(Type.getType(StringBuilder.class),
                Method.getMethod("StringBuilder append (String)"));
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.invokeVirtual(Type.getType(StringBuilder.class),
                Method.getMethod("StringBuilder append (String)"));
        mv.push("in " + visitedClassName + "$dispatch implementation, restart the application");
        mv.invokeVirtual(Type.getType(StringBuilder.class),
                Method.getMethod("StringBuilder append (String)"));

        mv.invokeVirtual(Type.getType(StringBuilder.class),
                Method.getMethod("String toString()"));

        // create the exception with the message
        mv.newInstance(INSTANT_RELOAD_EXCEPTION);
        mv.dupX1();
        mv.swap();
        mv.invokeConstructor(INSTANT_RELOAD_EXCEPTION,
                Method.getMethod("void <init> (String)"));
        // and throw.
        mv.throwException();

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        super.visitEnd();
    }

    private String getOverridenName(String methodName) {
        // TODO: change the method name as it can now collide with existing static methods with
        // the same signature.
        if (methodName.equals("<init>")) {
            return "init$override";
        }
        return methodName;
    }

    /**
     * Command line invocation entry point. Expects 2 parameters, first is the source directory
     * with .class files as produced by the Java compiler, second is the output directory where to
     * store the bytecode enhanced version.
     * @param args the command line arguments.
     * @throws IOException if some files cannot be read or written.
     */
    public static void main(String[] args) throws IOException {

        IncrementalVisitor.main(args, new VisitorBuilder() {
            @Override
            public IncrementalVisitor build(@NonNull ClassNode classNode,
                    List<ClassNode> parentNodes,
                    ClassVisitor classVisitor) {
                return new IncrementalChangeVisitor(classNode, parentNodes, classVisitor);
            }

            @Override
            public boolean processParents() {
                return true;
            }
        });
    }
}