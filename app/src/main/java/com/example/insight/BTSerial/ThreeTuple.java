package com.example.insight.BTSerial;

public class ThreeTuple{
   private int[] data;
   private int deadline;
   private int priority;

   public ThreeTuple(int[] data, int deadline, int priority){
       this.data = data;
       this.deadline = deadline;
       this.priority = priority;
   }

   public static int max(ThreeTuple[] tuples){
       int max = -1;
       for (ThreeTuple tuple : tuples){
           int temp  = tuple.getPriority();
           if (max < temp){
               max = temp;
           }
       }
       return max;
   }

   public static ThreeTuple CombineMap(ThreeTuple map, ThreeTuple second){
       int[] firstData = map.getData();
       int[] secondData = second.getData();
       int[] thirdData = new int[20];
       for (int i = 0; i < firstData.length; i++){
           if (firstData[i] == -1){
               if (secondData[i] == -1){
                   thirdData[i] = 0;
               }
               else{
                thirdData[i] = secondData[i];
               }
           }else if (secondData[i] == -1){
               thirdData[i] = firstData[i];
           }
           else{
               thirdData[i] = secondData[i];
           }
       }
       return null;
   }
   
   public int getPriority(){
       return priority;
   }
   public int[] getData(){
       return data;
   }
   public int getDeadline(){
       return deadline;
   }
}