import java.io.*;
/**
 * Class NLayer implements a network with one input layer, any number of hidden layers, and one output layer. It is
 * user configurable, with options for training and running modes, learning rate, maximum iterations, error threshold,
 * and saving weights. This class uses back propagation to train the network.
 * 
 * Table of Contents:
 * public static void setParam() throws Exception
 * public static void echoParam()
 * public static void allocMemory()
 * public static void populateArrays() throws Exception
 * public static void populateInputs()
 * public static void populateTruthTable()
 * public static void randomizeWeights()
 * public static double randomize(double low, double high)
 * public static double activationFunction(double x)
 * public static double activationFunctionDerivative (double x)
 * public static double sigmoid(double x)
 * public static double sigmoidDerivative(double x)
 * public static double tanh(double x)
 * public static double tanhDerivative(double x)
 * public static void train()
 * public static void loadInputs(int c)
 * public static double trainRun(int c)
 * public static void reportTrain(double time, boolean maxIterationsReached, boolean averageErrorReached, int iterations,
 *                                double avgError)
 * public static void run()
 * public static void runRun()
 * public static void reportRun(double time)
 * public static void printTime(double seconds)
 * public static void saveWeights() throws Exception
 * public static void loadWeights() throws Exception
 * public static void saveInputs() throws Exception
 * public static void main(String[] args) throws Exception
 * 
 * @author  Iris Wang
 * @version April 29, 2026
 */
public class NLayer
{
   private static int[] layerSizes;
   private static int numCases;

   private static int INPUTLAYER = 0;
   private static int HIDDENLAYER1 = 1;
   private static int outputlayer;
   private static int N_ACTLAYERS;
   private static int N_CONLAYERS;

   private static boolean trainingMode;
   private static boolean runningMode;

   private static double lambda;
   private static int maxIterations;
   private static double errorThreshold;

   private static double randLow;
   private static double randHigh;

   private static double[][] activations;
   private static double[][][] weights;

   private static double[][] theta;
   private static double[][] psi;

   private static double[][] inputCases;
   private static double[][] truthTable;
   private static double[][] actualOutputs;

   private static boolean randWeights;

   private static double MILLISECOND = 1000.0;
   private static double SECOND = 1.0;
   private static double MINUTE = 60.0;
   private static double HOUR = 60.0;
   private static double DAY = 24.0;
   private static double WEEK = 7.0;

   private static String saveFile;
   private static String loadFile;
   private static boolean save;
   private static boolean printInput;
   private static boolean printTruth;

   private static String controlFile;
   private static String inputFile;
   private static String inputFileSave;
   private static String truthTableFile;
   private static String CONTROL_DEFAULT = "configFileNLayer.txt";
   

   private static int keepAlive;

/**
 * Sets configuration parameters and is controlled by the control file.
 */
   public static void setParam() throws Exception
   {
      Parser parser = new Parser(controlFile);

      layerSizes = parser.getValIntArray("layers");
      N_ACTLAYERS = layerSizes.length;
      N_CONLAYERS = N_ACTLAYERS - 1;
      outputlayer = N_ACTLAYERS - 1;

      numCases = parser.getValInt("numCases");

      lambda = parser.getValDouble("lambda");
      maxIterations = parser.getValInt("maxIterations");
      errorThreshold = parser.getValDouble("errorThreshold");

      randLow = parser.getValDouble("randLow");
      randHigh = parser.getValDouble("randHigh");

      trainingMode = parser.getValBoolean("trainingMode");
      runningMode = parser.getValBoolean("runningMode");

      randWeights = parser.getValBoolean("randomizeWeights");

      inputFile = parser.getValString("inputFile");

      if (trainingMode)
      {
         truthTableFile = parser.getValString("truthTableFile");
      }
      
      printInput = parser.getValBoolean("printInput");

      if (trainingMode)
      {
         printTruth = parser.getValBoolean("printTruth");
      }

      save = parser.getValBoolean("save");
      if (save)
      {
         saveFile = parser.getValString("saveFile");
      }

      loadFile = parser.getValString("loadFile");

      keepAlive = parser.getValInt("keepAlive");
   } // public static void setParam()

/**
 * Prints out network parameters.
 */
   public static void echoParam()
   {
      System.out.println("Configuration file name: " + controlFile);
      System.out.println("Network configuration: ");

      for (int n = INPUTLAYER; n < N_ACTLAYERS - 1; n++)
      {
         System.out.print(layerSizes[n] + "-");
      } // for (int n = INPUTLAYER; n < N_ACTLAYERS; n++)
      System.out.println(layerSizes[outputlayer]);

      if (randWeights)
      {
         System.out.println("Weights are randomized.");
      }
      else
      {
         System.out.println("Weights are loaded from " + loadFile);
      }

      if (trainingMode)
      {
         System.out.println("Training mode on.");
         System.out.println("Random number range: [" + randLow + ", " + randHigh + "]");
         System.out.println("Max iterations: " + maxIterations);
         System.out.println("Error threshold: " + errorThreshold);
         System.out.println("Lambda value: " + lambda);
      } // if (trainingMode)

      if (runningMode)
      {
         System.out.println("Running mode on.");
      }

      System.out.println("Input file name: " + inputFile);
      if (trainingMode)
      {
         System.out.println("Truth table file name: " + truthTableFile);
      }
      
      if (save)
      {
         System.out.println("Weights are saved to " + saveFile);
      }

      System.out.println("Keep alive: " + keepAlive);

      System.out.println();
   } // public static void echoParam()

/**
 * Allocates memory to necessary arrays based on configuration parameters.
 */
   public static void allocMemory()
   {
      activations = new double[N_ACTLAYERS][];
      for (int n = INPUTLAYER; n < N_ACTLAYERS; n++)
      {
         activations[n] = new double[layerSizes[n]];
      }

      weights = new double[N_CONLAYERS][][];
      for (int n = INPUTLAYER; n < N_CONLAYERS; n++)
      {
         weights[n] = new double[layerSizes[n]][layerSizes[n + 1]];
      }

      inputCases = new double[numCases][layerSizes[INPUTLAYER]];

      if (trainingMode)
      {
         theta = new double[N_ACTLAYERS - 1][];
         for (int n = HIDDENLAYER1; n < N_ACTLAYERS - 1; n++)
         {
            theta[n] = new double[layerSizes[n]];
         }

         psi = new double[N_ACTLAYERS][];
         for (int n = HIDDENLAYER1; n < N_ACTLAYERS; n++)
         {
            psi[n] = new double[layerSizes[n]];
         }

         truthTable = new double[numCases][layerSizes[outputlayer]];
      } // if (trainingMode)

      if (runningMode)
      {
         actualOutputs = new double[numCases][layerSizes[outputlayer]];
      }
   } // public static void allocMemory()

/**
 * Populates weight, input, and truth table arrays.
 */
   public static void populateArrays() throws Exception
   {
      populateInputs();
      if (trainingMode)
      {
         populateTruthTable();
      }
      if (randWeights)
      {
         randomizeWeights();
      }
      else
      {
         loadWeights();
      }
   } // public static void populateArrays() throws Exception

/**   
 * Populates the inputs based on the input file.
 */
   public static void populateInputs()
   {
      try
      {
         java.io.DataInputStream in = new java.io.DataInputStream(new java.io.BufferedInputStream(
                                                                  new java.io.FileInputStream(inputFile)));

         for (int c = 0; c < numCases; c++)
         {
            int n = INPUTLAYER;
            for (int k = 0; k < layerSizes[n]; k++)
            {
               inputCases[c][k] = in.readDouble();
            }
         } // for (int c = 0; c < numCases; c++)

         in.close();
      } // try
      catch (IOException e)
      {
         System.out.println("Unable to read input file " + inputFile);
         e.printStackTrace();
         System.exit(1);
      }
   } // public static void populateInputTable()

/**
 * Populates the inputs based on the truth table file.
 */
   public static void populateTruthTable()
   {
      try
      {
         java.io.BufferedReader reader =  new java.io.BufferedReader(new java.io.FileReader(truthTableFile));

         int c = 0;
         String line = reader.readLine();

         while (line != null && c < numCases)
         {
            line = line.trim();
            if (!line.isEmpty())
            {
               String[] vals = line.split("\\s+");

               int n = outputlayer;
               for (int i = 0; i < layerSizes[n]; i++)
               {
                  truthTable[c][i] = Double.parseDouble(vals[i]);
               }

               c++;
            } // if (!line.isEmpty())

            line = reader.readLine();
         } // while (line != null && c < numCases)

         reader.close();
      } // try 
      catch (IOException e)
      {
         System.out.println("Unable to read truth table file " + truthTableFile);
         e.printStackTrace();
         System.exit(1);
      }
   } // public static void populateTruthTable()

/**
 * Populates weight arrays using randomization. The range of randomization
 * is from randLow to randHigh.
 */
   public static void randomizeWeights()
   {
      for (int n = INPUTLAYER; n < N_CONLAYERS; n++)
      {
         for (int k = 0; k < layerSizes[n]; k++)
         {
            for (int j = 0; j < layerSizes[n + 1]; j++)
            {
               weights[n][k][j] = randomize(randLow, randHigh);
            }
         }
      } // for (int n = INPUTLAYER; n < N_CONLAYERS; n++)
   } // public static void randomizeWeights()

/**
 * Returns a randomized value based on low and high bounds.
 * 
 * @param low  low bound for randomization
 * @param high high bound for randomization
 * @return  randomized value
 */
   public static double randomize(double low, double high)
   {
      return low + Math.random() * (high - low);
   }

/**
 * Returns the value outputted from the activation function.
 * 
 * @param x the input value
 * @return  the value the activation function gives
 */
   public static double activationFunction(double x)
   {
      return sigmoid(x);
   }
   
/**
 * Returns the value outputted from the derivative of the activation function.
 * 
 * @param x the input value
 * @return  the value the derivate of the activation function gives
 */
   public static double activationFunctionDerivative (double x)
   {
      return sigmoidDerivative(x);
   }

/**
 * Returns the value outputted from the sigmoid function.
 * 
 * @param x the input value
 * @return  the value the sigmoid function gives
 */
   public static double sigmoid(double x)
   {
      return 1.0 / (1.0 + Math.exp(-x));
   }

/**
 * Returns the value outputted from the derivative of the sigmoid  function.
 * 
 * @param x the input value
 * @return  the value the derivate of the sigmoid function gives
 */
   public static double sigmoidDerivative(double x)
   {
      double f = sigmoid(x);
      return f * (1.0 - f);
   }

/**
 * Returns the value outputted from the hyperbolic tangent function.
 * 
 * @param x the input value
 * @return  the value the hyperbolic tangent function gives
 */
   public static double tanh(double x)
   {
      double eps = (x < 0.0) ? 1.0 : -1.0;
      double exp = Math.exp(eps * 2.0 * x);
      return eps * (exp - 1.0) / (exp + 1.0);
   }

/**
 * Returns the value outputted from the derivative of the hyperbolic tangent function.
 * 
 * @param x the input value
 * @return  the value the derivate of the hyperbolic tangent function gives
 */
   public static double tanhDerivative(double x)
   {
      double f = tanh(x);
      return 1.0 - f * f;
   }

/**
 * Trains the network using steepest descent training.
 */
   public static void train()
   {
      int iterations = 0;
      boolean maxIterationsReached = false;
      boolean averageErrorReached = false;

      double totalDoubleError = 0.0;
      double avgError = 0.0;

      double startTime = System.currentTimeMillis();
      
      while (!maxIterationsReached && !averageErrorReached)
      {
         totalDoubleError = 0.0;

         for (int c = 0; c < numCases; c++)
         {
            loadInputs(c);

            totalDoubleError += trainRun(c);

            for (int n = outputlayer - 1; n > HIDDENLAYER1; n--)
            {
               for (int j = 0; j < layerSizes[n]; j++)
               {
                  double omegaj = 0.0;
                  for (int i = 0; i < layerSizes[n + 1]; i++)
                  {
                     omegaj += psi[n + 1][i] * weights[n][j][i];
                     weights[n][j][i] += lambda * activations[n][j] * psi[n + 1][i];
                  }

                  psi[n][j] = omegaj * activationFunctionDerivative(theta[n][j]);
               } // for (int j = 0; j < numHidden; j++)
            } // for (int n = outputlayer - 1; n > numHidden1; n--)

            int n = HIDDENLAYER1;
            for (int k = 0; k < layerSizes[n]; k++)
            {
               double omegak = 0.0;
               for (int j = 0; j < layerSizes[n + 1]; j++)
               {
                  omegak += psi[n + 1][j] * weights[n][k][j];
                  weights[n][k][j] += lambda * activations[n][k] * psi[n + 1][j];
               }

               double psik = omegak * activationFunctionDerivative(theta[n][k]);

               for (int m = 0; m < layerSizes[n - 1]; m++)
               {
                  weights[n - 1][m][k] += lambda * activations[n - 1][m] * psik;
               }
            } // for (int k = 0; k < numHidden1; k++)
         } // for (int c = 0; c < numCases; c++)

         double totalError = totalDoubleError / 2.0;
         avgError = totalError / (double) numCases;
         iterations++;

         if (avgError < errorThreshold)
         {
            averageErrorReached = true;
         }

         if (iterations >= maxIterations)
         {
            maxIterationsReached = true;
         }

         if ((keepAlive != 0) && ((iterations % keepAlive) == 0))
         {
            System.out.printf("Iteration %d, Error = %f\n", iterations, avgError);
         }
      } // while (!endTrain)
      
      double endTime = System.currentTimeMillis();
      double time = (endTime - startTime) / MILLISECOND;
      System.out.println();
      reportTrain(time, maxIterationsReached, averageErrorReached, iterations, avgError);
   } // public static void train()

/**
 * Fills the activation values based on input case values.
 * 
 * @param c the index of the input case
 */
   public static void loadInputs(int c)
   {
      int n = INPUTLAYER;
      for (int k = 0; k < layerSizes[n]; k++)
      {
         activations[n][k] = inputCases[c][k];
      }
   } // public static void loadInputs(int c)

/**
 * Runs the network for training. Calculates layer values and returns twice the error.
 * 
 * @param c input case index
 * @return  twice the error
 */
   public static double trainRun(int c)
   {
      double totalDoubleError = 0.0;

      for (int n = HIDDENLAYER1; n < N_ACTLAYERS - 1; n++)
      {
         for (int j = 0; j < layerSizes[n]; j++)
         {
            theta[n][j] = 0.0;
            for (int k = 0; k < layerSizes[n - 1]; k++)
            {
               theta[n][j] += activations[n - 1][k] * weights[n - 1][k][j];
            }
            activations[n][j] = activationFunction(theta[n][j]);
         } // for (int j = 0; j < layerSizes[n]; j++)
      } // for (int n = HIDDENLAYER1; n < N_ACTLAYERS - 1; n++)

      int n = outputlayer;
      for (int i = 0; i < layerSizes[n]; i++)
      {
         double thetai = 0.0;
         for (int j = 0; j < layerSizes[n - 1]; j++)
         {
            thetai += activations[n - 1][j] * weights[n - 1][j][i];
         }
         activations[n][i] = activationFunction(thetai);

         double omegai = truthTable[c][i] - activations[n][i];
         psi[n][i] = omegai * activationFunctionDerivative(thetai);
         totalDoubleError += omegai * omegai;
      } // for (int i = 0; i < layerSizes[n]; i--)

      return totalDoubleError;
   } // public static double trainRun(int c)

/**
 * Prints out training results and reasons for exiting training.
 */
   public static void reportTrain(double time, boolean maxIterationsReached, boolean averageErrorReached, int iterations,
                                  double avgError)
   {
      System.out.println("Training complete!");
      System.out.println("Training Exit Information:");

      if (maxIterationsReached)
      {
         System.out.println("Reason for exit: Max iterations reached.");
      }

      if (averageErrorReached)
      {
         System.out.println("Reason for exit: Average error reached.");
      }

      System.out.println("Iterations reached: " + iterations);
      System.out.printf("Average error: %.4f", avgError);
      System.out.println();

      printInputTruth();

      System.out.println("\nTraining actual output: ");
      runAllCases();

      printTime(time);
      System.out.println();
   } // public static void reportTrain()

/**
 * Prints the input and truth tables.
 */
   public static void printInputTruth()
   {
      if (printInput)
      {
         System.out.print("Input table. ");
      }

      if (printTruth)
      {
         System.out.print("Truth table. ");
      }
      
      System.out.println();

      for (int c = 0; c < numCases; c++)
      {
         int n;
         if (printInput)
         {
            n = INPUTLAYER;
            for (int k = 0; k < layerSizes[n]; k++)
            {
               System.out.print(inputCases[c][k] + " ");
            }
            System.out.print("    ");
         }
         
         if (printTruth)
         {
            n = outputlayer;
            for (int i = 0; i < layerSizes[n]; i++)
            {
               System.out.printf("%.2f ", truthTable[c][i]);
            }
            System.out.println();
         }
      } // for (int c = 0; c < numCases; c++)
   }

/**
 * Runs all test cases.
 */
   public static void runAllCases()
   {
      int n = outputlayer;

      for (int c = 0; c < numCases; c++)
      {
         loadInputs(c);
         runRun();

         for (int i = 0; i < layerSizes[n]; i++)
         {
            System.out.printf("%.2f ", activations[n][i]);
         }

         System.out.println();
      } // for (int c = 0; c < numCases; c++)
   } // public void runAllCases()

/**
 * Runs and tests the network.
 */
   public static void run()
   {
      double startTime = System.currentTimeMillis();

      for (int c = 0; c < numCases; c++)
      {
         loadInputs(c);
         runRun();

         int n = outputlayer;
         for (int i = 0; i < layerSizes[n]; i++)
         {
            actualOutputs[c][i] = activations[n][i];
         }
      } // for (int c = 0; c < numCases; c++)

      double endTime = System.currentTimeMillis();
      double time = (endTime - startTime) / MILLISECOND;
      reportRun(time);
   } // public static void run()

/**
 * Calculates the hidden layer values and the output value.
 */
   public static void runRun()
   {
      for (int n = HIDDENLAYER1; n < N_ACTLAYERS; n++)
      {
         for (int j = 0; j < layerSizes[n]; j++)
         {
            double thetaRun = 0.0;
            for (int k = 0; k < layerSizes[n - 1]; k++)
            {
               thetaRun += activations[n - 1][k] * weights[n - 1][k][j];
            }
            activations[n][j] = activationFunction(thetaRun);
         } // for (int j = 0; j < layerSizes[n]; j++)
      } // for (int n = HIDDENLAYER1; n < N_ACTLAYERS; n++)
   } // public static void runRun()

/**
 * Reports results of running.
 */
   public static void reportRun(double time)
   {
      System.out.println("Running complete!");
      if (printInput)
      {
         System.out.println("Input table and actual output:");
      }
      else
      {
         System.out.println("Actual output");
      }
     
      for (int c = 0; c < numCases; c++)
      {
         int n;
         if (printInput)
         {
            n = INPUTLAYER;
            for (int k = 0; k < layerSizes[n]; k++)
            {
               System.out.print(inputCases[c][k] + " ");
            }
            System.out.print("    ");
         }
         
         n = outputlayer;
         for (int i = 0; i < layerSizes[n]; i++)
         {
            System.out.printf("%.2f ", actualOutputs[c][i]);
         }
         System.out.println();
      } // for (int c = 0; c < numCases; c++)

      printTime(time);
   } // public static void reportRun()

/**
 * Accepts a value representing seconds elapsed and prints out a decimal value
 * in easier to digest units.
 * 
 * @param seconds the seconds elapsed
 */
   public static void printTime(double seconds)
   {
      double minutes, hours, days, weeks;

      System.out.print("Elapsed time: ");

      if (seconds < SECOND)
      {
         System.out.printf("%.4f", seconds * MILLISECOND);
         System.out.print(" milliseconds");
      }
      else if (seconds < MINUTE)
      {
         System.out.printf("%.4f", seconds);
         System.out.print(" seconds");
      }
      else
      {
         minutes = seconds / MINUTE;

         if (minutes < HOUR)
         {
            System.out.printf("%.4f", minutes);
            System.out.print(" minutes");
         }
         else
         {
            hours = minutes / HOUR;

            if (hours < DAY)
            {
               System.out.printf("%.4f", hours);
               System.out.print(" hours");
            }
            else
            {
               days = hours / DAY;

               if (days < WEEK)
               {
                  System.out.printf("%.4f", days);
                  System.out.print(" days");
               }
               else
               {
                  weeks = days / WEEK;
                  System.out.printf("%.4f", weeks);
                  System.out.print(" weeks");
               }
            } // if (hours < DAY)...else
         } // if (minutes < HOUR)...else
      } // else if (seconds < MINUTE)...else

      System.out.println();
      return;
   } // public static void printTime(double seconds)

/**
 * Saves weights to the weights file.
 */
   public static void saveWeights() throws Exception
   {
      DataOutputStream out = new DataOutputStream(new FileOutputStream(saveFile));
      
      for (int n = INPUTLAYER; n < N_ACTLAYERS; n++)
      {
         out.writeInt(layerSizes[n]);
      }

      for (int n = INPUTLAYER; n < N_CONLAYERS; n++)
      {
         for (int k = 0; k < layerSizes[n]; k++)
         {
            for (int j = 0; j < layerSizes[n + 1]; j++)
            {
               out.writeDouble(weights[n][k][j]);
            }
         }
      } // for (int n = INPUTLAYER; n < N_CONLAYERS; n++)
      
      out.close();
   } // public static void saveWeights() throws Exception

/**
 * Loads weights through the weights file.
 */
   public static void loadWeights() throws Exception
   {
      DataInputStream in = new DataInputStream(new FileInputStream(loadFile));
      
      for (int n = INPUTLAYER; n < N_ACTLAYERS; n++)
      {
         int size = in.readInt();
         if (size != layerSizes[n])
         {
            in.close();
            throw new Exception("Weight file configuration mismatch. The wrong file is loaded.");
         }
      } // for (int n = INPUTLAYER; n < N_ACTLAYERS; n++)

      for (int n = INPUTLAYER; n < N_CONLAYERS; n++)
      {
         for (int k = 0; k < layerSizes[n]; k++)
         {
            for (int j = 0; j < layerSizes[n + 1]; j++)
            {
               weights[n][k][j] = in.readDouble();
            }
         }
      } // for (int n = INPUTLAYER; n < N_CONLAYERS; n++)
      
      in.close();
   } // public static void loadWeights() throws Exception

/**
 * Saves inputs to an input file.
 */
   public static void saveInputs() throws Exception
   {
      DataOutputStream out = new DataOutputStream(new FileOutputStream(inputFileSave));

      for (int c = 0; c < numCases; c++)
      {
         int n = INPUTLAYER;
         for (int i = 0; i < layerSizes[n]; i++)
         {
            out.writeDouble(inputCases[c][i]);
         }
      } // for (int c = 0; c < numCases; c++)

      out.close();
   } // public static void saveInputs() throws Exception

   public static void main(String[] args) throws Exception
   {
      controlFile = CONTROL_DEFAULT;
      if (args.length > 0)
      {
         controlFile = args[0];
      }

      setParam();
      echoParam();
      allocMemory();
      populateArrays();

      if (trainingMode)
      {
         train();
      }

      if (runningMode)
      {
         run();
      }

      if (save)
      {
         saveWeights();
      }
   } // public static void main(String[] args) throws Exception
} // public class NLayer