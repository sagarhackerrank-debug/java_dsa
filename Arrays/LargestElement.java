//array basics 


public class LargestElement {
    public static void main(String[] args) {

        int[] arr = {10, 25, 7, 42, 18};

        int largest = arr[0];
        int smallest = arr[0];


        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > largest) {
                largest = arr[i];
            }
        
  
        if(arr[i]<smallest){
            smallest=arr[i];
}
}
        System.out.println("Largest element = " + largest);
         System.out.print("smallest element = " + smallest);
    }
}
