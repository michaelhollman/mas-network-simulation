library(ggplot2)

baseline <- read.csv("C:\\Users\\Scott Johnson\\Documents\\MAS Final Project\\BaseLine\\BaselineNonAggregateNetworkData.2015.Apr.30.14_36_47-pass2.csv")
leafultra <- read.csv("C:\\Users\\Scott Johnson\\Documents\\MAS Final Project\\LeafAndUltra\\LeafAndUltraNonAggregateNetworkData.2015.Apr.30.14_50_17-pass2.csv")
highChurn <- read.csv("C:\\Users\\Scott Johnson\\Documents\\MAS Final Project\\HighChurn\\HighChurnNonAggregateNetworkData.2015.Apr.30.14_39_00-pass2.csv")
lowChurn <- read.csv("C:\\Users\\Scott Johnson\\Documents\\MAS Final Project\\LowChurn\\LowChurnNonAggregateNetworkData.2015.Apr.30.14_42_15-pass2.csv")
highConnection <- read.csv("C:\\Users\\Scott Johnson\\Documents\\MAS Final Project\\HighConnection\\HighConnectionNonAggregateNetworkData.2015.Apr.30.14_44_53-pass2.csv")
lowConnection <- read.csv("C:\\Users\\Scott Johnson\\Documents\\MAS Final Project\\LowConnection\\LowConnectionNonAggregateNetworkData.2015.Apr.30.14_46_48-pass2.csv")
highPing <- read.csv("C:\\Users\\Scott Johnson\\Documents\\MAS Final Project\\PingHigh\\PingHighNonAggregateNetworkData.2015.Apr.30.14_53_17-pass2.csv")
lowPing <- read.csv("C:\\Users\\Scott Johnson\\Documents\\MAS Final Project\\PingLow\\PingLowNonAggregateNetworkData.2015.Apr.30.14_57_46-pass2.csv")

compareAvgFulTimeLeaf <- function(df1, df2, df3, title1, title2, title3, title){
  temp1 <- df1[which(df1[,3] == FALSE | df1[,3] == "False"), c(2, 4)]
  temp1$Legend <- title1
  temp2 <- df2[which(df2[,3] == FALSE | df2[,3] == "False"), c(2, 4)]
  temp2$Legend <- title2
  temp3 <- df3[which(df3[,3] == FALSE | df3[,3] == "False"), c(2, 4)]
  temp3$Legend <- title3
  tempTotal <- rbind(temp1, temp2, temp3)
  ggplot(tempTotal, aes(x=Tick, y=AvgFulfillmentTime)) + geom_line(aes(color=Legend)) + ggtitle(title)
}

compareFulTimeout <- function(df1, boolUltra1, textUltra1, df2, boolUltra2, textUltra2, df3, boolUltra3, textUltra3, title1, title2, title3, title){
  ful1 <- df1[which(df1[,3] == boolUltra1 | df1[,3] == textUltra1), c(2, 5)]
  colnames(ful1)[2] <- "Value"
  ful1$Legend <- paste(title1, "Fulfilled", sep = " ")
  tim1 <- df1[which(df1[,3] == boolUltra1 | df1[,3] == textUltra1), c(2, 8)]
  colnames(tim1)[2] <- "Value"
  tim1$Legend <- paste(title1, "Timeouts", sep = " ")
  ful2 <- df2[which(df2[,3] == boolUltra2 | df2[,3] == textUltra2), c(2, 5)]
  colnames(ful2)[2] <- "Value"
  ful2$Legend <- paste(title2, "Fulfilled", sep = " ")
  tim2 <- df2[which(df2[,3] == boolUltra2 | df2[,3] == textUltra2), c(2, 8)]
  colnames(tim2)[2] <- "Value"
  tim2$Legend <- paste(title2, "Timeouts", sep=" ")
  ful3 <- df3[which(df3[,3] == boolUltra3 | df3[,3] == textUltra3), c(2, 5)]
  colnames(ful3)[2] <- "Value"
  ful3$Legend <- paste(title3, "Fulfilled", sep = " ")
  tim3 <- df3[which(df3[,3] == boolUltra3 | df3[,3] == textUltra3), c(2, 8)]
  colnames(tim3)[2] <- "Value"
  tim3$Legend <- paste(title3, "Timeouts", sep=" ")
  mygraphdata <- rbind(ful1, tim1, ful2, tim2, ful3, tim3)
  ggplot(mygraphdata, aes(x=Tick, y=Value)) + geom_line(aes(color=Legend)) + ggtitle(title)
}

compareVariances <- function(df1, boolUltra1, textUltra1, df2, boolUltra2, textUltra2, df3, boolUltra3, textUltra3, title1, title2, title3, title){
  var1 <- df1[which(df1[,3] == boolUltra1 | df1[,3] == textUltra1), c(2, 11)]
  var1$Legend <- title1
  var2 <- df2[which(df2[,3] == boolUltra2 | df2[,3] == textUltra2), c(2, 11)]
  var2$Legend <- title2
  var3 <- df3[which(df3[,3] == boolUltra3 | df3[,3] == textUltra3), c(2, 11)]
  var3$Legend <- title3
  mygraphdata <- rbind(var1, var2, var3)
  ggplot(mygraphdata, aes(x=Tick, y=QueueVariance)) + geom_line(aes(color=Legend)) + ggtitle(title)
}

LeafUltraQueue <- function(df1, title){
  expL <- df1[which(df1[,3] == FALSE), c(2, 10)]
  expL$Legend <- "Leaf avg queue size"
  expU <- df1[which(df1[,3] == TRUE), c(2, 10)]
  expU$Legend <- "Ultra avg queue size"
  mygraphdata <- rbind(expL, expU)
  ggplot(mygraphdata, aes(x=Tick, y=AvgWorkQueueSize)) + geom_line(aes(color=Legend)) + ggtitle(title)
}

LeafUltraVar <- function(df1, title){
  varL <- df1[which(df1[,3] == FALSE), c(2, 11)]
  varL$Legend <- "Leaf queue variance"
  varU <- df1[which(df1[,3] == TRUE), c(2, 11)]
  varU$Legend <- "Ultra queue variance"
  mygraphdata <- rbind(varL, varU)
  ggplot(mygraphdata, aes(x=Tick, y=QueueVariance)) + geom_line(aes(color=Legend)) + ggtitle(title)
}

ggplot(baseline, aes(x=Tick, y=AvgFulfillmentTime)) + geom_line() + ggtitle("Exp 1: Avg Fulfillment Time")
ggplot(baseline, aes(x=Tick, y=QueueVariance)) + geom_line() + ggtitle("Exp 1: Queue Variance")

compareAvgFulTimeLeaf(baseline, leafultra, "Experiment 1", "Experiment 2", "Exp 2: Avg Fulfillment Time")
compareFulTimeout(baseline, FALSE, leafultra, FALSE, "Experiment 1", "Experiment 2", "Exp 2: Fulfills Vs Timeouts")
compareVariances(baseline, FALSE, leafultra, FALSE, "Experiment 1", "Experiment 2", "Exp 2: Queue Variance")

compareAvgFulTimeLeaf(leafultra, highChurn, lowChurn, "Experiment 2", "High Churn", "Low Churn", "Exp 4: Avg Fulfillment Time")
compareFulTimeout(leafultra, FALSE, "False", highChurn, FALSE, "False", lowChurn, FALSE, "False", "Experiment 2", "High Churn", "Low Churn", "Exp 4: Fulfills Vs Timeouts")
compareVariances(leafultra, FALSE, "False", highChurn, FALSE, "False", lowChurn, FALSE, "False", "Experiment 2", "High Churn", "Low Churn", "Exp 4: Queue Variance")

compareAvgFulTimeLeaf(baseline, highConnection, lowConnection, "Experiment 1", "High Connection", "Low Connection", "Exp 3: Avg Fulfillment Time")
compareFulTimeout(baseline, FALSE, "False", highConnection, FALSE, "False", lowConnection, FALSE, "False", "Experiment 1", "High Connection", "Low Connection", "Exp 3: Fulfills Vs Timeouts")
compareVariances(baseline, FALSE, "False", highConnection, FALSE, "False", lowConnection, FALSE, "False", "Experiment 1", "High Connection", "Low Connection", "Exp 3: Queue Variance")

compareAvgFulTimeLeaf(leafultra, highPing, lowPing, "Experiment 2", "High Ping", "Low Ping", "Exp 5: Avg Fulfillment Time")
compareFulTimeout(leafultra, FALSE, "False", highPing, FALSE, "False", lowPing, FALSE, "False", "Experiment 2", "High Ping", "Low Ping", "Exp 5: Fulfills Vs Timeouts")
compareVariances(leafultra, FALSE, "False", highPing, FALSE, "False", lowPing, FALSE, "False", "Experiment 2", "High Ping", "Low Ping", "Exp 5: Queue Variance")
