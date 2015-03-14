# Sample JPA Entity #

```
@Entity
@Table(name="mytable")
public class MyEntityimplements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="ID", unique=true, nullable=false)
  private int id;

  @Column(name="Text")
  private String text;

  public MyEntity() {
  }

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getText() {
    return this.text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
```

# Creating New Record #

```
JpaHelper db = new JpaHelper("MyPersistenceUnitName");

MyEntity my = new MyEntity();
my.setText("bla bla bla ...");

db.create(my);
```

# Updating Record #

```
JpaHelper db = new JpaHelper("MyPersistenceUnitName");

MyEntity my = db.find(MyEntity.class, 1);
my.setText("that's interesting!");
db.update(my);
```

# Deleting Record #

```
JpaHelper db = new JpaHelper("MyPersistenceUnitName");

db.delete(db.find(MyEntity.class, 1));
```