
	• Login - Private key to access is attached.
	VM Instance: 20.4.49.213
	Username: azureuser
	
	• Go to project path
	cd  /home/azureuser/group/info-retrieval-group-project
	
	• Build - build project
	mvn package
	
	• Run -this will generate the top file: 'RUN5.txt'
	 java -jar target/assignment_2-1.0-SNAPSHOT.jar
	
	• Trec evaluation
	../../trec_eval/trec_eval qrel.txt RUN5.txt
	
	
1.to run the run5(best) setup and create index, use cmd:
# java -jar target/assxx.jar 5 c 
2.to run the run5(best) setup without create index, use cmd:
# java -jar target/assxx.jar 5
or
# java -jar target/assxx.jar 


