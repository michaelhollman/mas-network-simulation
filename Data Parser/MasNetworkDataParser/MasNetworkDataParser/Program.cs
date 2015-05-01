using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace MasNetworkDataParser
{
    class Program
    {
        static void Main(string[] args)
        {
            if (args.Length != 1 || string.IsNullOrEmpty(args[0]))
            {
                Console.WriteLine("Usage: MasNetworkDataParser.exe inputFilePath");
                return;
            }

            var inputFilePath = args[0];
            var inputFileWithoutCsv = inputFilePath.Substring(0, inputFilePath.Length - 4);

            var pass1Path = inputFileWithoutCsv + "-pass1.csv";
            var pass2Path = inputFileWithoutCsv + "-pass2.csv";

            Pass1(inputFilePath, pass1Path);
            Pass2(pass1Path, pass2Path);

            Console.WriteLine("Done");
        }

        static void Pass1(string inputFilePath, string outputFilePath)
        {
            const int numNodes = 100;

            using (var streamReader = new StreamReader(inputFilePath))
            {
                using (var streamWriter = new StreamWriter(outputFilePath))
                {
                    // Strip header row
                    var line = streamReader.ReadLine();

                    // Write header row in output
                    streamWriter.WriteLine("Run,Tick,IsUltra,AvgFulfillmentTime,NumFulfilled,AvgKnownFiles,AvgKnownNodes," +
                                           "NumTimeouts,PercentFulfilled,AvgWorkQueueSize,QueueVariance,NumberDead,Count");

                    // Loop over Runs, Ticks, and Nodes.  Pass 1 aggregates nodes per tick
                    for (var runIterator = 0; runIterator < 30; runIterator++)
                    {
                        for (var tickIterator = 0; tickIterator < 10000; tickIterator++)
                        {
                            var tickData = new TickData { WorkQueueObservations = new List<int>(), IsUltra = false };
                            var ultraTickData = new TickData { WorkQueueObservations = new List<int>(), IsUltra = true };

                            var hadUltras = false;

                            for (var nodeIterator = 0; nodeIterator < numNodes; nodeIterator++)
                            {
                                line = streamReader.ReadLine();
                                if (line == null) break;

                                var parts = line.Split(',');
                                var run = int.Parse(parts[0]);
                                var avgFulfillmentTime = double.Parse(parts[1]);
                                var numFulfilled = int.Parse(parts[2]);
                                var numKnownFiles = int.Parse(parts[3]);
                                var numKnownNodes = int.Parse(parts[4]);
                                var numTimeouts = int.Parse(parts[5]);
                                var numUnfulfilled = int.Parse(parts[6]);
                                var percentFulfilled = double.Parse(parts[7]);
                                var workQueueSize = int.Parse(parts[8]);
                                var isDead = bool.Parse(parts[9].Replace("\"", " "));
                                var isUltra = bool.Parse(parts[10].Replace("\"", " "));
                                var tickDouble = double.Parse(parts[11]);
                                var tick = (int)tickDouble;

                                if (isUltra)
                                {
                                    hadUltras = true;

                                    ultraTickData.Run = run;
                                    ultraTickData.Tick = tick;

                                    ultraTickData.Count++;
                                    ultraTickData.AverageFulfillmentTime += avgFulfillmentTime;
                                    ultraTickData.NumberFullfilledRequests += numFulfilled;
                                    ultraTickData.AverageKnownFiles += numKnownFiles;
                                    ultraTickData.AverageKnownNodes += numKnownNodes;
                                    ultraTickData.NumberTimeouts += numTimeouts;
                                    ultraTickData.AverageWorkQueueSize += workQueueSize;
                                    ultraTickData.NumberDead += isDead ? 1 : 0;
                                    ultraTickData.WorkQueueObservations.Add(workQueueSize);
                                }
                                else
                                {
                                    tickData.Run = run;
                                    tickData.Tick = tick;

                                    tickData.Count++;
                                    tickData.AverageFulfillmentTime += avgFulfillmentTime;
                                    tickData.NumberFullfilledRequests += numFulfilled;
                                    tickData.AverageKnownFiles += numKnownFiles;
                                    tickData.AverageKnownNodes += numKnownNodes;
                                    tickData.NumberTimeouts += numTimeouts;
                                    tickData.AverageWorkQueueSize += workQueueSize;
                                    tickData.NumberDead += isDead ? 1 : 0;
                                    tickData.WorkQueueObservations.Add(workQueueSize);
                                }
                            }
                            if (line == null) break;

                            // Compute appropriate averages and summaries, and write to file

                            tickData.AverageFulfillmentTime = 1.0 * tickData.AverageFulfillmentTime / tickData.Count;
                            tickData.AverageKnownFiles = 1.0 * tickData.AverageKnownFiles / tickData.Count;
                            tickData.AverageKnownNodes = 1.0 * tickData.AverageKnownNodes / tickData.Count;
                            tickData.AverageWorkQueueSize = 1.0 * tickData.AverageWorkQueueSize / tickData.Count;

                            var denom = (tickData.NumberFullfilledRequests + tickData.NumberTimeouts);
                            if (denom == 0) denom = 1;
                            tickData.PercentFulfilled = (1.0 * tickData.NumberFullfilledRequests) / denom;
                            tickData.WorkQueueVariance = Variance(tickData.WorkQueueObservations, tickData.AverageWorkQueueSize);
                            WriteLine(tickData, streamWriter);

                            if (hadUltras)
                            {
                                ultraTickData.AverageFulfillmentTime = 1.0 * ultraTickData.AverageFulfillmentTime / ultraTickData.Count;
                                ultraTickData.AverageKnownFiles = 1.0 * ultraTickData.AverageKnownFiles / ultraTickData.Count;
                                ultraTickData.AverageKnownNodes = 1.0 * ultraTickData.AverageKnownNodes / ultraTickData.Count;
                                ultraTickData.AverageWorkQueueSize = 1.0 * ultraTickData.AverageWorkQueueSize / ultraTickData.Count;

                                var denomU = (ultraTickData.NumberFullfilledRequests + ultraTickData.NumberTimeouts);
                                if (denomU == 0) denomU = 1;
                                ultraTickData.PercentFulfilled = (1.0 * ultraTickData.NumberFullfilledRequests) / denomU;
                                ultraTickData.WorkQueueVariance = Variance(ultraTickData.WorkQueueObservations, ultraTickData.AverageWorkQueueSize);
                                WriteLine(ultraTickData, streamWriter);
                            }


                            if (tickIterator % 1000 == 0)
                            {
                                Console.WriteLine("Finished Tick" + tickIterator);
                            }
                        }
                        if (line == null) break;

                        Console.WriteLine("Finished Run" + runIterator);
                    }
                }
            }
        }

        static void Pass2(string inputFilePath, string outputFilePath)
        {
            using (var streamReader = new StreamReader(inputFilePath))
            {
                using (var streamWriter = new StreamWriter(outputFilePath))
                {
                    // Strip header row
                    var line = streamReader.ReadLine();

                    // Write header row in output
                    streamWriter.WriteLine("Run,Tick,IsUltra,AvgFulfillmentTime,NumFulfilled,AvgKnownFiles,AvgKnownNodes," +
                                           "NumTimeouts,PercentFulfilled,AvgWorkQueueSize,QueueVariance,NumberDead,Count");

                    var aggregateTickData = new TickData[10000];
                    var ultraAggregateTickData = new TickData[10000];
                    var hadUltras = false;
                    for (var i = 0; i < aggregateTickData.Length; i++)
                    {
                        aggregateTickData[i] = new TickData { IsUltra = false };
                        ultraAggregateTickData[i] = new TickData { IsUltra = true };
                    }

                    while (true)
                    {
                        line = streamReader.ReadLine();
                        if (line == null) break;

                        // Parse each line into tickdata
                        var parts = line.Split(',');

                        var run = int.Parse(parts[0]);
                        var tick = int.Parse(parts[1]);
                        var tickIndex = tick - 1;
                        var isUltra = bool.Parse(parts[2]);

                        if (isUltra)
                        {
                            hadUltras = true;
                            ultraAggregateTickData[tickIndex].Tick = tick;
                            ultraAggregateTickData[tickIndex].AverageFulfillmentTime += double.Parse(parts[3]);
                            ultraAggregateTickData[tickIndex].NumberFullfilledRequests += int.Parse(parts[4]);
                            ultraAggregateTickData[tickIndex].AverageKnownFiles += double.Parse(parts[5]);
                            ultraAggregateTickData[tickIndex].AverageKnownNodes += double.Parse(parts[6]);
                            ultraAggregateTickData[tickIndex].NumberTimeouts += int.Parse(parts[7]);
                            ultraAggregateTickData[tickIndex].PercentFulfilled += double.Parse(parts[8]);
                            ultraAggregateTickData[tickIndex].AverageWorkQueueSize += double.Parse(parts[9]);
                            ultraAggregateTickData[tickIndex].WorkQueueVariance += double.Parse(parts[10]);
                            ultraAggregateTickData[tickIndex].NumberDead += int.Parse(parts[11]);

                            ultraAggregateTickData[tickIndex].Count += int.Parse(parts[12]);
                        }
                        else
                        {
                            aggregateTickData[tickIndex].Tick = tick;
                            aggregateTickData[tickIndex].AverageFulfillmentTime += double.Parse(parts[3]);
                            aggregateTickData[tickIndex].NumberFullfilledRequests += int.Parse(parts[4]);
                            aggregateTickData[tickIndex].AverageKnownFiles += double.Parse(parts[5]);
                            aggregateTickData[tickIndex].AverageKnownNodes += double.Parse(parts[6]);
                            aggregateTickData[tickIndex].NumberTimeouts += int.Parse(parts[7]);
                            aggregateTickData[tickIndex].PercentFulfilled += double.Parse(parts[8]);
                            aggregateTickData[tickIndex].AverageWorkQueueSize += double.Parse(parts[9]);
                            aggregateTickData[tickIndex].WorkQueueVariance += double.Parse(parts[10]);
                            aggregateTickData[tickIndex].NumberDead += int.Parse(parts[11]);

                            aggregateTickData[tickIndex].Count += int.Parse(parts[12]);
                        }

                    }


                    // Compute averages
                    for (var i = 0; i < aggregateTickData.Length; i++)
                    {
                        aggregateTickData[i].AverageFulfillmentTime = 1.0 * aggregateTickData[i].AverageFulfillmentTime / 30;
                        aggregateTickData[i].AverageKnownFiles = 1.0 * aggregateTickData[i].AverageKnownFiles / 30;
                        aggregateTickData[i].AverageKnownNodes = 1.0 * aggregateTickData[i].AverageKnownNodes / 30;
                        aggregateTickData[i].PercentFulfilled = 1.0 * aggregateTickData[i].PercentFulfilled / 30;
                        aggregateTickData[i].AverageWorkQueueSize = 1.0 * aggregateTickData[i].AverageWorkQueueSize / 30;
                        aggregateTickData[i].WorkQueueVariance = 1.0 * aggregateTickData[i].WorkQueueVariance / 30;

                        WriteLine(aggregateTickData[i], streamWriter);
                    }



                    if (hadUltras)
                    {
                        for (var i = 0; i < ultraAggregateTickData.Length; i++)
                        {
                            ultraAggregateTickData[i].AverageFulfillmentTime = 1.0 * ultraAggregateTickData[i].AverageFulfillmentTime / 30;
                            ultraAggregateTickData[i].AverageKnownFiles = 1.0 * ultraAggregateTickData[i].AverageKnownFiles / 30;
                            ultraAggregateTickData[i].AverageKnownNodes = 1.0 * ultraAggregateTickData[i].AverageKnownNodes / 30;
                            ultraAggregateTickData[i].PercentFulfilled = 1.0 * ultraAggregateTickData[i].PercentFulfilled / 30;
                            ultraAggregateTickData[i].AverageWorkQueueSize = 1.0 * ultraAggregateTickData[i].AverageWorkQueueSize / 30;
                            ultraAggregateTickData[i].WorkQueueVariance = 1.0 * ultraAggregateTickData[i].WorkQueueVariance / 30;

                            WriteLine(ultraAggregateTickData[i], streamWriter);
                        }
                    }


                }
            }
        }

        private static double Variance(List<int> numbers, double mean)
        {
            var result = numbers.Sum(number => Math.Pow(number - mean, 2.0));
            if (numbers.Count - 1 == 0) return 0;
            return result / (numbers.Count - 1);
        }

        private static void WriteLine(TickData data, StreamWriter writer)
        {
            writer.WriteLine("{0},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11},{12}",
                data.Run, data.Tick, data.IsUltra,
                data.AverageFulfillmentTime,
                data.NumberFullfilledRequests,
                data.AverageKnownFiles,
                data.AverageKnownNodes,
                data.NumberTimeouts,
                data.PercentFulfilled,
                data.AverageWorkQueueSize,
                data.WorkQueueVariance,
                data.NumberDead,
                data.Count);
        }
    }

    public struct TickData
    {
        // Metadata
        public int Run { get; set; }
        public int Tick { get; set; }
        public bool IsUltra { get; set; }

        // Statistics
        public double AverageFulfillmentTime { get; set; }
        public int NumberFullfilledRequests { get; set; }
        public double AverageKnownFiles { get; set; }
        public double AverageKnownNodes { get; set; }
        public int NumberTimeouts { get; set; }
        public double PercentFulfilled { get; set; }
        public double AverageWorkQueueSize { get; set; }
        public double WorkQueueVariance { get; set; }
        public int NumberDead { get; set; }

        // For compilation
        public int Count { get; set; }
        public List<int> WorkQueueObservations { get; set; }
    }
}
