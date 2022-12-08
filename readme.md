# Instructions

1. Go to project path on cmd

2. To build project on cmd, run:

```
mvn package
```	

3. Results file Creation

To create an index and get the results file of the best run, run:

```
java -jar target/assignment_2-1.0-SNAPSHOT.jar 7 c
```

To get the results file of the best run wihtout creating an index, run:

```
java -jar target/assignment_2-1.0-SNAPSHOT.jar
```

OR

```
java -jar target/assignment_2-1.0-SNAPSHOT.jar 7
```

4. For Trec evaluation results, run:

```
../../trec_eval/trec_eval qrel.txt results.txt
```	


