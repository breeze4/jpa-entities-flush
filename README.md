# jpa-entities-flush
Demonstrating a bug when flushing an entity with a generated payload and losing data that one might expect to keep on the object before an explicit save.


To use:
Clone the repository and then with Maven:

  `mvn install`
  
  `mvn -Dtest=Proof test`

To get the test to pass, comment out or delete line 52 in ViewEntity where dirty = false is set.

See src/test/com/entities/proof/Proof.java for more words.
