import java.util.Scanner;
public class don2{
public static void main(String args[]){

Scanner in=new Scanner(System.in);
System.out.println( "enter a size");
int n=in.nextInt();
int pro=1;

int[] arr=new int[n];
System.out.println("enter the elemets");


for(int i=0;i<n;i++)
 arr[i]=in.nextInt();

for(int i=0;i<n;i++)

pro=pro*arr[i];

System.out.print(pro);



}


}








