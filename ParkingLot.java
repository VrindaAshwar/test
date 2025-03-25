public class ParkingLot{
    private ParkingSpace[] spaces;
    private String lotName;
    private String lotAvailability;
    private boolean enabled;


    public ParkingLot(String lotName) {
        this.lotName = lotName;
        this.enabled = true;            //enabled by default
        spaces = new ParkingSpace[100]; //lots always have 100 spaces as per the assigment guidelines
        for (int i = 0; i < spaces.length; i++) {
            spaces[i] = new ParkingSpace(i, "EmptyState", "Empty", "Empty");
        }
    }

    public ParkingSpace[] getSpaces(){
        return spaces;
    }

    public boolean getEnabled(){
        return enabled;
    }
    public String getLotName(){
        return lotName;
    }
    public ParkingSpace getSpace(int index){
        return spaces[index];
    }
    //this needs the observer stuff
    public String getLotAvailability(){
        return lotAvailability;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public void setSpace(int index, String state, String user, String car){
        this.spaces[index] = new ParkingSpace(index, state, user, car);
    }
}
