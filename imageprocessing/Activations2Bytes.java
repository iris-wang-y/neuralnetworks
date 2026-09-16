/**
**
** Bytes2Activations.java
**
** @author Eric R. Nelson
** May 11, 2026
**
** This code reads in a binary file of activation values (0.0 to 1.0) and converts them into a byte file (0 to 255)
** This code is needed since the C and Java double precision formats are not the same
**
** Search for $$$ to change the data scaling by the program.
**
** javac Activations2Bytes.java
**
** Usage:
** java Activations2Bytes input_activation_file output_byte_file
**
** If two arguments are not passed, then a usage message is presented to the user.
**
**/
import java.io.*;

public class Activations2Bytes
   {
   static final double SCALE  = 255.0; // $$$  double value = byte * SCALE + OFFSET
   static final double OFFSET = 0.0;   // $$$ 

   public static void main(String[] args)
      {
      byte byteVal;
      int intVal;
      double activationVal;
/*
** args[0] - input bin file
** args[1] - output activation file
**
** The default size for the student photos in IC is 175x232 (width, height)
**
*/
      if (args.length != 2)
         System.out.printf("Usage: java Activations2Bytes input_activation_file output_byte_file\n\n");
      else
         {
         String inFileName  = args[0];
         String outFileName = args[1];

         System.out.printf("Reading file '%s' and writing out '%s'.\n", inFileName, outFileName);

         try // lots of things can go wrong when doing file i/o
            {
            FileInputStream fInStream = new FileInputStream(inFileName); // Open the file that is the second command line parameter
            DataInputStream in = new DataInputStream(fInStream);         // Convert our input stream to a DataInputStream

            try
               {
               FileOutputStream fOutStream = new FileOutputStream(outFileName);
               DataOutputStream out = new DataOutputStream(fOutStream);
/*
** Read in 1 double value and then write them out as scaled byte values
*/
               boolean EOF = false;
               while (!EOF)
                  {
                  try // an exception is thrown when end of file is reached
                     {
                     activationVal = in.readDouble();
                     intVal = (int)(activationVal * SCALE + OFFSET);
                     byteVal = (byte)intVal;

                     out.writeByte(byteVal);
                     } // try
                  catch (Exception e)
                     {
                     EOF = true;
                     }
                  } // while (!EOF)

               out.close();
               fOutStream.close();
               } // try
            catch (Exception e)
               {
               System.err.println("File output error" + e);
               }

            in.close();
            fInStream.close();
            } // try
         catch (Exception e)
            {
            System.err.println("File input error" + e);
            }
         } // if (args.length != 2) ... else
      } // public public void main
   } // public class Activations2Bytes
