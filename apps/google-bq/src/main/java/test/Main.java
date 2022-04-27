package test;

import java.io.Console;  
public class Main {  
  public static void main(String[] args) throws Exception {  
 Console obj = System.console();  
  if (obj != null) {  
      String fmt = "%1$4s %2$10s %3$10s%n";  
 // format  
      obj.printf(fmt, "cse", "program", "language");  
      obj.printf(fmt, "-----", "-----", "-----");  
      obj.printf(fmt, "PHP", "Java", "Python");  
      obj.printf(fmt, "CSS", "Html", "JavaScript");  
     }   }  
}  
