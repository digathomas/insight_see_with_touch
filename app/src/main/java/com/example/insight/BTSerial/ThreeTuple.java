package com.example.insight.BTSerial;

import java.time.Instant;

public class ThreeTuple<T>{
   private T data;
   private Instant deadline;
   private int priority;

   public ThreeTuple(T data, Instant deadline, int priority){
       this.data = data;
       this.deadline = deadline;
       this.priority = priority;
   }

   public static int max(ThreeTuple[] tuples){
       int max = -1;
       //ThreeTuple maxTuple = null;
       for (ThreeTuple tuple : tuples){
           if (tuple == null) continue;
           int temp  = tuple.getPriority();
           if (max < temp){
               //maxTuple = tuple;
               max = temp;
           }
       }
       return max;
   }

//   public static ThreeTuple CombineMap(ThreeTuple map, ThreeTuple second){
//       int[] firstData = map.getData();
//       int[] secondData = second.getData();
//       int[] thirdData = new int[20];
//       for (int i = 0; i < firstData.length; i++){
//           if (firstData[i] == -1){
//               if (secondData[i] == -1){
//                   thirdData[i] = 0;
//               }
//               else{
//                thirdData[i] = secondData[i];
//               }
//           }else if (secondData[i] == -1){
//               thirdData[i] = firstData[i];
//           }
//           else{
//               thirdData[i] = secondData[i];
//           }
//       }
//       return null;
//   }
   
   public int getPriority(){
       return priority;
   }
   public T getData(){
       return data;
   }
   public Instant getDeadline(){
       return deadline;
   }
}