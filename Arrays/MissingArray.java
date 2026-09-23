//array missing array

import java.util.Arrays;
public class MissingArray{
public static void main(String[]args){

//Scanner in=new Scanner(System.in);
//System.out.println( "enter a size");
//int n=in.nextInt();
int arraysum=0;

int arr[]={1,3,4,5,6,7};
int n=arr.length+1;

int sum=n*(n+1)/2;
for(int i=0;i<arr.length;i++)
arraysum=arraysum+arr[i];

int missarray=sum-arraysum;

System.out.print(missarray+" ");

}
}








