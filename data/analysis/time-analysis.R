data <- read.csv(file="/home/loopasam/git/chembl-annotator/data/times/agaulton@ebi.ac.uk.csv", 
                 head=FALSE, 
                 sep="\t", 
                 quote = "\'");
# Plot the war data
boxplot(data$V1)

# Identify outliers and get rid of them
times <- data$V1[!data$V1 %in% boxplot.stats(data$V1)$out]
hist(times, breaks=20, col="red")