This contains a C# app that takes as input the `csv` text sinks generated by Repast batch runs. Each batch run generates about 2 GB of raw data and this parser aggregates it down to between 1 and 2 MB of data that we then use for graphing and visuals. So for our paper, we went from roughly 16 GB of data to 20 MB, which was much easier to work with.

Please note that the code included in this tool was created as an ad-hoc tool and is not set up to be conveniently run elsewhere. The code is only included for completeness.
