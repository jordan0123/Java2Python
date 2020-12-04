public class CheckPrime {
      public static void main(String[] args){
            int m = 0, flag = false;
            int n = 29; //checks this number
            m = n/2;
            
            if(n == 0 || n == 1){
                 System.out.println(n + " is NOT a prime number");
            }
            else{
                  for(int i = 2; i <= m; i++){
                        if(n % i == 0){
                            System.out.println(n + " is NOT a prime number");
                            flag = true;
                            break;
                        }
                  }
                if(flag == false){
                   System.out.println(n + " is a prime number"); 
                }
            }
      }
}