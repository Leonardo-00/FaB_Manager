package Utils.constants;

public class IdGenerator {

    private int nextId;

    public IdGenerator(int id){
        nextId = id;
    }

    public IdGenerator(){
        this(1);
    }

    public IdGenerator(IdGenerator idGen){
        this.nextId = idGen.nextId;
    }

    public int getNextId(){
        return nextId++;
    }

    public void setNextId(int id){
        nextId = id;
    }
}

