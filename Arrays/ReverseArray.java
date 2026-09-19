//reverse array


public class ReverseArray{
public static void main(String args[]){

int arr[]={20, 90, 10 ,50, 40, 60};
int n=arr.length;
int i=0;int j=n-1;
int temp;
//Scanner in=new Scanner(System.in);
//System.out.println( "enter a size");
//int n=in.nextInt();


while(i<j){
temp=arr[i];
arr[i]=arr[j];
arr[j]=temp;      
i++;
j--;

}
for( i=0;i<n;i++)
System.out.print(arr[i]+" ");
}
}


