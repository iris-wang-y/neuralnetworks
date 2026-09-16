
import java.io.*;
import java.util.*;
/**
 * Parses the configuration file for the N-Layer network.
 * 
 * Table of Contents:
 * public Parser(String filename) throws IOException
 * public static void parse(String filename) throws IOException
 * public String getValString(String key)
 * public int[] getValIntArray(String key)
 * public boolean getValBoolean(String key)
 * public int getValInt(String key)
 * public double getValDouble(String key)
 *
 * @author  Iris Wang
 * @version March 25, 2026
 */
public class Parser
{
   private static Map<String, String> params;

/**
 * Constructor for objects of the class Parser.
 * 
 * @param filename   the name of the configuration file
 */
   public Parser(String filename) throws IOException
   {
      params = new HashMap<>();
      parse(filename);
   }

/**
 * Parses the configuration file by extracting key-value pairs and storing them in a map of parameters.
 * 
 * @param filename   the name of the configuration file
 */
   public static void parse(String filename) throws IOException
   {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      
      String line = reader.readLine();
      while (line != null)
      {
         line = line.trim();
         if (!line.isEmpty())
         {
            int split = line.indexOf('=');
            if (split >= 0)
            {
               String key = line.substring(0, split).trim();
               String val = line.substring(split + 1).trim();
               params.put(key, val);
            }
         } // if (!line.isEmpty())

         line = reader.readLine();
      } // while (line != null)

      reader.close();
   } // public static void parse(String filename) throws IOException
 
/**
 * Returns the value for the given key as a string.
 * 
 * @param key  the configuration key
 * @return  the value for the key as a string
 */
   public String getValString(String key)
   {
      String val = params.get(key);
      if (val == null)
      {
         throw new RuntimeException("Missing value for key " + key);
      }
 
      return val;
   } // public String getString(String key)

/**
 * Returns the value for the given key as an array of integers.
 * 
 * @param key  the configuration key
 * @return  the value for the key as a string
 */
   public int[] getValIntArray(String key)
   {
      String val = getValString(key);

      String[] tokens = val.split("[,\\s]+");

      int[] result = new int[tokens.length];

      for (int i = 0; i < tokens.length; i++)
      {
         result[i] = Integer.parseInt(tokens[i]);
      }

      return result;
   } // public int[] getValIntArray(String key)

/**
 * Returns the value for the given key as a boolean.
 * 
 * @param key  the configuration key
 * @return  the value for the key as a boolean
 */
   public boolean getValBoolean(String key)
   {
      return Boolean.parseBoolean(getValString(key));
   }
 
/**
 * Returns the value for the given key as an integer.
 * 
 * @param key  the configuration key
 * @return  the value for the key as an integer
 */
   public int getValInt(String key)
   {
      return Integer.parseInt(getValString(key));
   }
 
/**
 * Returns the value for the given key as a double.
 * 
 * @param key  the configuration key
 * @return  the value for the key as a double
 */
   public double getValDouble(String key)
   {
      return Double.parseDouble(getValString(key));
   }
} // public class Parser