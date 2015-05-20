# TwitterTrend
Uses Hadoop MapReduce to comb through nearly 5GB of tweets to find what users like to talk most about (in English). 

#### Step 0: Cleaning up the 5GB tweet file
The 5GB dataset for this project contained many lines of "Twitter exception: Connection failed" and other system errors embedded among the tweets. Since the goal of this project was to form a trending set only for English language sentences, the data set had to be cleaned up for foreign language sentences and "bad" lines which were not tweets. 

In order to do so, a python file DataCleaner.py has been provided. DataCleaner.py filters out all lines in the original data set beginning with '@', performs a maximum likelhood estimate of whether or not the line belongs to the English language (because even Spanish/Swedish orthography can be rendered using English language characters) and writes them out to a file called final_cleanfile.txt. 

#### Step 1: Configuring Hadoop and testing MapReduce on the system locally
This project installs Hadoop on Ubuntu 14.0.1, and then configures the core-site.xml, HDFS-site.xml and map-reduce.xml to get a local namenode and a datanode running. Video link to the same. The output is available locally and appears to be working as required. 

#### Step 2: Deploying this app using Amazon Elasting MapReduce (EMR)
While deploying, I discovered that Amazon EMR uses Java 7 (update 71) whereas I was using Java 8. So I downgraded the existing projetc to Java 7 (update 80). Also, while I was running the code locally, I could read in files from the local filesystem. However, in EMR that facility does not exist. So the code was modified to be able to read in from the Amazon S3 bucket. The commonwords.txt was retained in the local filesystem and read into Hadoop file system when the map reduce program run.
