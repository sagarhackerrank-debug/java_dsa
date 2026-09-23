// product of 3 largest elemets in array

import java.util.Arrays;
public class proOfLargestthree{
public static void main(String[]args){

//Scanner in=new Scanner(System.in);
//System.out.println( "enter a size");
//int n=in.nextInt();
//int arraysum=0;



int arr[]={1,3,4,5,6};
int n=arr.length;
int max=arr[0];
int smax=Integer.MIN_VALUE;

int tmax=Integer.MIN_VALUE;


for(int i=0;i<n;i++){

if(arr[i]>max){

tmax=smax;
smax=max;

max=arr[i];
}
else if(arr[i]>smax && arr[i]!=max){
tmax=smax;
smax=arr[i];

}
else if(arr[i]>tmax && arr[i]!=max && arr[i]!=smax){
tmax=arr[i];
}

}
System.out.print(max*smax*tmax +" ");
}
}





