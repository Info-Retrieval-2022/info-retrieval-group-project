
	• Login - Private key to access is attached.
	VM Instance: 20.4.49.213
	Username: azureuser
	
	• Go to project path
	cd  /home/azureuser/group/info-retrieval-group-project
	
	• Build - build project
	mvn package
	
	• Run -this will generate the top file: 'RUN7.txt'
	 java -jar target/assignment_2-1.0-SNAPSHOT.jar 7
	
	• Trec evaluation
	../../trec_eval/trec_eval qrel.txt RUN7.txt
	
	
1.to run the run7(best) setup and create index, use cmd:
# java -jar target/assxx.jar 7 c 
2.to run the run7(best) setup without create index, use cmd:
# java -jar target/assxx.jar 7
or
# java -jar target/assxx.jar 


