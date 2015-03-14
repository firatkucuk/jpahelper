Jpa Helper is a tiny class helps users to create, update, delete and edit database records easily and may update related collections via using java reflection api.

# Creating New Record #

```
JpaHelper db = new JpaHelper("puMyPersistenceUnit");

MyEntity my = new MyEntity();
my.setText("bla bla bla ...");

db.create(my);
```

# Updating Record #

```
JpaHelper db = new JpaHelper("puMyPersistenceUnit");
MyEntity  my = (MyEntity) db.find(MyEntity.class, 1);
my.setText("that's interesting!");
db.update(my);
```

# Deleting Record #

```
JpaHelper db = new JpaHelper("puMyPersistenceUnit");
MyEntity  my = (MyEntity) db.find(MyEntity.class, 1);
db.delete(my);
```