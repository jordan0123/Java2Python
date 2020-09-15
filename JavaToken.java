
public class JavaToken{
    String literal;
    String tokenName;
    int tokenCode;
    Boolean possMulti; // operator that may have another character ex. + could be ++ so true but [ or || are complete so false
    
    JavaToken(String literal, String tokenName, int tokenCode,Boolean possMulti){
        this.literal = literal;
        this.tokenName = tokenName;
        this.tokenCode = tokenCode;
        this.possMulti = possMulti;
    }
    
    JavaToken(String literal, String tokenName, int tokenCode){
        this.literal = literal;
        this.tokenName = tokenName;
        this.tokenCode = tokenCode;
        this.possMulti = false;
    }
              
    boolean isOperator(){
        return ( this.tokenCode >= 2000 && this.tokenCode <= 2999);
    }
    
    boolean isKeyword(){
        return (this.tokenCode >= 1000 && this.tokenCode <= 1999);
    }
              
    boolean possibleMulti(){
        return this.possMulti;
    }
    
    String tokenName(){
        return this.tokenName;
    }
    
    int tokenCode(){
        return this.tokenCode;
    }
}
