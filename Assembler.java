/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simulator;

/**
 *
 * @author saad
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import com.simulator.gui;

public class Assembler {
    static String[] RType={"add","sub","and","or","slt"};
    static String[] RTypeBinary={"100000","100010","100100","100101","101010"};
    static String[] IType={"addi","andi","ori","slti"};
    static String[] ITypeBinary={"001000","001100","001101","001010"};
    static String[] WType={"lw","sw"};
    static String[] WTypeBinary={"100011","101011"};
    static String[] BType={"beq","bne"};
    static String[] BTypeBinary={"000100","000101"};


    static HashMap<String,String> labels=new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        String input=scanner.nextLine();
        System.out.println(assembler(input,0));
    }
    public static ArrayList<String> assembler(ArrayList<String> input){
        ArrayList<String> res=new ArrayList<>();
        for(int i=0 ; i<input.size() ; i++){
            res.add(assembler(input.get(i),i));
        }
        return res;
    }
    public static String assembler(String input,int index){
        String[] pars=input.split(" ");       
        if(pars[0].charAt(pars[0].length()-1) == ':'){
            // it's a label
            labels.put(pars[0],getBinary(index*4));
            // deleting the first index
            String[] pars2=new String[pars.length-1];
            for(int i=1 ; i<pars.length ; i++){
                pars2[i-1]=pars[i];
            }
            pars=pars2;
        }
        String op=pars[0];
        if(op.equals("lui"))
        {
            ArrayList<String> tempArr = new ArrayList<>();
            String temp = pars[1];
            tempArr = parseReg(pars);            
            String res = "00111100000" + getRegister(tempArr.get(0))+ gui.convertToBinary(Integer.parseInt(pars[2]), 16);
            return res;
        }
        else
        {
            int ind=-1;
            // checking for rtype
            for (int i=0 ; i<RType.length ; i++) {
                if (op.equals(RType[i])) {
                    ind=i;
                }
            }
            if(ind!=-1){
                return "000000"+parseRtype(input)+RTypeBinary[ind];
            }
            // checking for itype
            for (int i=0 ; i<IType.length ; i++) {
                if (op.equals(IType[i])) {
                    ind=i;
                }
            }
            if(ind!=-1){
                return ITypeBinary[ind]+parseItype(input);
            }
            // checking for wtype
            for (int i=0 ; i<WType.length ; i++) {
                if (op.equals(WType[i])) {
                    ind=i;
                }
            }
            if(ind!=-1){
                return WTypeBinary[ind]+parseWtype(input);
            }
            // checking for btype
            for (int i=0 ; i<BType.length ; i++) {
                if (op.equals(BType[i])) {
                    ind=i;
                }
            }
            if(ind!=-1){
                return BTypeBinary[ind]+parseWtype(input);
            }
            // sll instruction
            if(op.equals("sll")){
                ArrayList<String> regs=parseReg(pars);
                return "00000000000"+getRegister(regs.get(1))+getRegister(regs.get(0))
                        +getInSize(getBinary(Integer.parseInt(pars[pars.length-1])),5)
                        +"000000";
            }
            // j instruction
            if(op.equals("j")){
                return "000010"+labels.get(pars[pars.length-1]);
            }
            if(op.equals("jr")){
                return "000000"+getRegister(pars[pars.length-1])+"000000000000000001000";
            }
        }
        return null;
    }
    static ArrayList<String> parseReg(String[] pars){
        ArrayList<String> regs=new ArrayList<>();
        for(int i=0 ; i<pars.length ; i++){
            pars[i]=pars[i].trim();     // eliminates leading and trailing spaces
            if(pars[i].charAt(0)=='$'){
                if(pars[i].endsWith(",")){
                    pars[i]=pars[i].substring(0,pars[i].length()-1);
                }
                regs.add(pars[i]);
            }
        }
        return regs;
    }
 
    static String parseBtype(String input){
        String[] pars=input.split(" ");
        ArrayList<String> reg=parseReg(pars);
        return getRegister(reg.get(0))+getRegister(reg.get(1))+
                getInSize(labels.get(pars[pars.length-1]),16);
    }
    static String parseWtype(String input){
        // lw $t2, 32($t3)
        StringBuilder inputArr=new StringBuilder();
        for(int i=0 ; i<input.length() ; i++){
            if(input.charAt(i)=='(' || input.charAt(i)==')'){
                inputArr.append(' ');
                inputArr.append(input.charAt(i));
                inputArr.append(' ');
            }else {
                inputArr.append(input.charAt(i));
            }
        }
        input=inputArr.toString();
        String[] pars=input.split(" ");
        ArrayList<String> regs=parseReg(pars);
        int num=-1;
        for(String str:pars){
            try {
                num=Integer.parseInt(str);
                break;
            }catch (Exception ignored){

            }
        }
        return getRegister(regs.get(1))+getRegister(regs.get(0))+getInSize(getBinary(num),16);                
    }

    static String parseRtype(String input){
        String[] pars=input.split(" ");
        ArrayList<String> regs=parseReg(pars);
        return getInSize(getRegister(regs.get(1)),5)+
                getInSize(getRegister(regs.get(2)),5)+
                getInSize(getRegister(regs.get(0)),5) +
                "00000";
    }
    static String parseItype(String input){
        String[] pars=input.split(" ");
        ArrayList<String> regs=parseReg(pars);
        boolean isNeg=false;
        int num=Integer.parseInt(pars[pars.length-1]);
        return getInSize(getRegister(regs.get(1)),5)+
                getInSize(getRegister(regs.get(0)),5)+
                (num>=0 ? getInSize(getBinary(num),16)
                        : twosComplement(getInSize(getBinary(num*-1),16)));
    }
    static String twosComplement(String bin){
        boolean foundOne=false;
        char[] res=new char[bin.length()];
        for(int i=bin.length()-1 ; i>=0 ; --i){
            if(foundOne){
                if(bin.charAt(i)=='1')res[i]='0';
                else res[i]='1';
                continue;
            }
            if(bin.charAt(i)=='1') foundOne=true;
            res[i]=bin.charAt(i);
        }
        return String.valueOf(res);
    }
    static String getBinary(int n){
        String binary="";
        while(n>0){
            if(n%2==1)binary+='1';
            else binary+='0';
            n/=2;
        }
        String res="";
        for(int i=binary.length()-1 ; i>=0 ; i--){
            res+=binary.charAt(i);
        }
        return res;
    }
    static String getInSize(String str,int sz){
        int diff=sz-str.length();
        for(int i=0 ; i<diff ; i++){
            str='0'+str;
        }
        return str;
    }
    static String getRegister(String reg){
        reg=reg.trim();
        if(reg.equals("$r0")){
            return "00000";       
        }else if(reg.equals("$at")){
            return "00001";       
        }else if(reg.equals("$v0")){
            return "00010";
        }else if(reg.equals("$v1")){
            return "00011";
        }else if(reg.equals("$a0")){
            return "00100";
        }else if(reg.equals("$a1")){
            return "00101";
        }else if(reg.equals("$a2")){
            return "00110";
        }else if(reg.equals("$a3")){
            return "00111";
        }else if(reg.equals("$t0")){
            return "01000";
        }else if(reg.equals("$t1")){
            return "01001";
        }else if(reg.equals("$t2")){
            return "01010";
        }else if(reg.equals("$t3")){
            return "01011";
        }else if(reg.equals("$t4")){
            return "01100";
        }else if(reg.equals("$t5")){
            return "01101";
        }else if(reg.equals("$t6")){
            return "01110";
        }else if(reg.equals("$t7")){
            return "01111";
        }else if(reg.equals("$s0")){
            return "10000";
        }else if(reg.equals("$s1")){
            return "10001";
        }else if(reg.equals("$s2")){
            return "10010";
        }else if(reg.equals("$s3")){
            return "10011";
        }else if(reg.equals("$s4")){
            return "10100";
        }else if(reg.equals("$s5")){
            return "10101";
        }else if(reg.equals("$s6")){
            return "10110";
        }else if(reg.equals("$s7")) {
            return "10111";
        }else if(reg.equals("$t8")){
            return "11000";
        }else if(reg.equals("$t9")) {
            return "11001";
        }
        else if(reg.equals("$k0")) {
            return "11010";
        }
        else if(reg.equals("$k1")) {
            return "11011";
        }
        else if(reg.equals("$gp")) {
            return "11100";
        }
        else if(reg.equals("$sp")) {
            return "11101";
        }
        else if(reg.equals("$s8")) {
            return "11110";
        }
        else if(reg.equals("$ra")) {
            return "11111";
        }
        return "";
    }
}
// sll $t0, $s1, 4
// 00000000000100010100000100000000
// ox00114100
/*
add $t2, $t0, $t1
sll $t2, $t1, 4
addi $t2, $t0, 6
*/


//00100001000010100000000000000110
//ox210a0006

//lw $t2, 32($t3)
//10001101011010100000000000100000
