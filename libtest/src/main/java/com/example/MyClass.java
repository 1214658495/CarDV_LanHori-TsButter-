package com.example;

//public class MyClass {
////    private
//    public static void main(String[] arg) {
//         String name,name1,time;
//        int size;
//
////
//////        [{"2017-09-13-16-31-27.MP4":"188743680 bytes|2017-09-13 16:33:26"},{"2017-09-13-16-35-27.MP4":"188743680 bytes|2017-09-13 16:37:26"}]
////        String descriptor = "{\"2017-09-13-16-31-27.MP4\":\"188743680 bytes|2017-09-13 16:33:26\"}";
////        descriptor = descriptor.replaceAll("[{\"}]", "");
////        int index = descriptor.indexOf(":");
////        name = descriptor.substring(0, index);
////
////        descriptor = descriptor.substring(index + 1);
////        index = descriptor.indexOf(" ");
////        if (descriptor.contains("|")) {
////            size = Integer.parseInt(descriptor.substring(0, index));
////
////            index = descriptor.indexOf("|");
////            time = descriptor.substring(index + 1);
////        } else if (descriptor.contains("bytes")) {
////            size = Integer.parseInt(descriptor.substring(0, index));
////            time = null;
////        } else {
////            size = -1;
////            time = descriptor.substring(index + 1);
////        }
//
//
//    }
//}

//public class MyClass {
//    public static void main(String[] args) {
//        System.out.println("我是main方法，我输出Super的类变量i：" + Sub.i);
//        Sub sub = new Sub();
//    }
//}
//
//class Super {
//    {
//        System.out.println("我是Super成员块");
//    }
//
//    public Super() {
//        System.out.println("我是Super构造方法");
//    }
//
//    {
//        int j = 123;
//        System.out.println("我是Super成员块中的变量j：" + j);
//    }
//
//    static {
//        System.out.println("我是Super静态块");
//        i = 123;
//    }
//
//    protected static int i = 1;
//}
//
//class Sub extends Super {
//    static {
//        System.out.println("我是Sub静态块");
//    }
//
//    public Sub() {
//        System.out.println("我是Sub构造方法");
//    }
//
//    {
//        System.out.println("我是Sub成员块");
//    }
//}


class Person {

//3.执行类中的静态代码块：如果有的话，对Person.class类进行初始化。
    static {
        System.out.println("静态代码块被执行");
    }

//4.开辟空间：在堆内存中开辟空间，分配内存地址。

//5.默认初始化：在堆内存中建立 对象的特有属性，并进行默认初始化。
//6.显示初始化：对属性进行显示初始化。
    private String name;
    private int age = 1;
    private static String country = "cn";

//7.构造代码块：执行类中的构造代码块，对对象进行构造代码块初始化。
    {
        System.out.println(name + "..." + age);
    }

//8.构造函数初始化：对对象进行对应的构造函数初始化。

    Person(String name, int age) {
        this.name = name;
        this.age = age;
        System.out.println("构造函数被执行");
    }
//9.将内存地址赋值给栈内存中的变量p

    public void setName(String name) {
        this.name = name;
    }

    public void speak() {
        System.out.println(this.name + "..." + this.age);
    }

    public static void showCountry() {
        System.out.println("country=" + country);
    }
}

class StaticDemo {
    static {
        System.out.println("StaticDemo 静态代码块1");
    }

    public static void main(String[] args) {
        Person p = new Person("zhangsan", 100);
//        1.在栈内存中，开辟main函数的空间，建立main函数的变量 p。
//        2.加载类文件：因为new要用到Person.class,所以要先从硬盘中找到Person.class类文件，并加载到内存中。
//        加载类文件时，除了非静态成员变量（对象的特有属性）不会被加载，其它的都会被加载。
//        注意：在Person.class文件加载时，静态方法和非静态方法都会加载到方法区中，只不过要调用到非静态方法时需要先实例化一个对象
//，对象才能调用非静态方法。如果让类中所有的非静态方法都随着对象的实例化而建立一次，那么会大量消耗内存资源，
//        所以才会让所有对象共享这些非静态方法，然后用this关键字指向调用非静态方法的对象。

        p.setName("lisi");
//        1.在栈内存中开辟setName方法的空间，里面有：对象的引用this，临时变量name
//        2.将p的值赋值给this,this就指向了堆中调用该方法的对象。
//        3.将"lisi" 赋值给临时变量name。
//        4.将临时变量的值赋值给this的name。

        p.speak();

        Person.showCountry();
//        1.在栈内存中，开辟showCountry()方法的空间，里面有：类名的引用Person。
//        2.Person指向方法区中Person类的静态方法区的地址。
//        3.调用静态方法区中的country，并输出。
//        注意：要想使用类中的成员，必须调用。通过什么调用？有：类名、this、super
    }

    static {
        System.out.println("StaticDemo 静态代码块2");
    }
}