//rotate array
import java.util.Arrays;
public class array9{
public static void main(String[]args){

//Scanner in=new Scanner(System.in);
//System.out.println( "enter a size");
//int n=in.nextInt();
int d=2;

int arr[]={20, 90, 10 ,50, 40, 60};
int n=arr.length;

reverse(arr,0,d-1);
reverse(arr,d,n-1);
reverse(arr,0,n-1);

for(int i=0;i<n;i++)
System.out.print(arr[i]+" ");


}
public static void reverse(int arr[],int i,int j){
while(i<j){
int temp=arr[i];
arr[i]=arr[j];
arr[j]=temp;
i++;
j--;
}

}
}


