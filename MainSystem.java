import java.util.ArrayList;
import java.io.FileWriter;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

//maybe treat this as a facade design pattern
public class MainSystem {
    private static MainSystem instance;
    //need to track who is logged in
    public static User currentUser;
    //file for storing user info
    public String userFilePath;
    //file for storing maanger accounts
    public String managerFilePath;
    //file for sotring lot info
    public String lotFilePath;
    public String parkingSpaceFilePath;
    private ArrayList<ParkingLot> lots = new ArrayList<>();
    private ArrayList<Manager> managers = new ArrayList<>();
    private ArrayList<User> approvedUsers = new ArrayList<>();
    private ArrayList<User> pendingUsers = new ArrayList<>();
    private UserFactory userFactory = new UserFactory();
    private MainSystem(){
        userFilePath = "data/userData.csv";
        managerFilePath = "data/managerData.csv";
        lotFilePath = "data/lotData.csv";
        parkingSpaceFilePath = "data/parkingSpaceData.csv";


        this.registerAccount("Visitor", "Josh", "j123");
        this.registerAccount("Student", "Ben", "b123");

        SuperManager sp = SuperManager.getInstance();
        this.managers.add(sp.createManagerAccount());
        this.managers.add(sp.createManagerAccount());
        this.managers.add(sp.createManagerAccount());

        this.lots.add(new ParkingLot("Lot 1"));
        this.lots.add(new ParkingLot("Lot 2"));
        this.lots.add(new ParkingLot("Lot 3"));

        updateFile( userFilePath );
        updateFile( managerFilePath );
        updateFile( lotFilePath );
        updateFile( parkingSpaceFilePath );

        try {
            loadFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static MainSystem getInstance(){
        if(instance == null){
            instance = new MainSystem();
        }
        return instance;
    }
    public ArrayList<ParkingLot> getLots(){
        return lots;
    }
    public ArrayList<Manager> getManagers(){
        return managers;
    }
    public ArrayList<User> getApprovedUsers(){
        return approvedUsers;
    }
    public ArrayList<User> getPendingUsers(){
        return pendingUsers;
    }

    public void registerAccount(String type,String username, String password){
        if (type.equals("Visitor")) { //visitor approved by default
            approvedUsers.add(userFactory.createUser(type, username, password));
        } else {
            pendingUsers.add(userFactory.createUser(type, username, password));
        }
    }
    public boolean isRegistered(String username){
    	//If a user is already registered (or pending), return true else return false
    	for (User user : approvedUsers) {
    		if (user.getUsername().equals(username)) {
    			return true;
    		}
    	}
    	//This is the condition we can decide to keep or remove depending on if we want to count pending users as registered.
    	for (User user : pendingUsers) {
    		if (user.getUsername().equals(username)) {
    			return true;
    		}
    	}
    	return false;
    }

    //below are methods for files
    //this loads data form the file into this classes lists
    //you wanna call this on program launch to get all data from the files into the porgram
    public void loadFiles() throws Exception{
        System.out.println("Loading files...");
        CsvReader reader = new CsvReader(userFilePath);
        reader.readHeaders();
        //users update
        while(reader.readRecord()){
            String type = reader.get("type");
            String username = reader.get("username");
            String approvalStatus = reader.get("approved");
            String password = reader.get("password");
            if(type.equals("Visitor")){//if visitor its auto approved
                approvedUsers.add(userFactory.createUser(type, username, password));
            }else{//if they are not a visitor check if they are aprroved or not
                if(approvalStatus.equals("true")){
                    approvedUsers.add(userFactory.createUser(type, username, password));
                }else{
                    pendingUsers.add(userFactory.createUser(type, username, password));
                }
            }
        }
        //managers update
        reader = new CsvReader(managerFilePath);
        while(reader.readRecord()){
            String username = reader.get("username");
            String password = reader.get("password");
            managers.add(new Manager(username, password));
        }
        //update lots
        reader = new CsvReader(lotFilePath);
        while(reader.readRecord()){
            System.out.println(reader.get("name"));
            String name = reader.get("name");
            ParkingLot lot = new ParkingLot(name);
            lot.setEnabled(reader.get("enabled").equals("true")); //make the lot enabled if it was enabled in the data sheet
            lots.add(lot);

        }
        //update spaces, will prolly edit later to also update user pesonal booking list
        reader = new CsvReader(parkingSpaceFilePath);
        System.out.println("Updating spaces...");
        while(reader.readRecord()){
            System.out.println(reader.get("lot") + reader.get("index"));
            String lotName = reader.get("lot");
            System.out.println(reader.get("lot") + " " + reader.get("index"));
            int index = Integer.parseInt(reader.get("index"));
            String state = reader.get("state");
            String user = reader.get("user");
            String car = reader.get("car");
            for (ParkingLot lot : lots) {
                if (lot.getLotName().equals(lotName)) {
                    lot.setSpace(index, state, user, car);
                    break;
                }
            }
        }
    }
    //this updates whatever file you call this method with
    //you wanna call this when new users or maanges are made, i will link this to gui
    public void updateFile(String path){
        System.out.println("Updating file: " + path);
        switch (path) {
            case "data/userData.csv":
                try{
                    CsvWriter csvOutput = new CsvWriter(new FileWriter(path, false), ',');
                    //email,password, approval status, type
                    csvOutput.write("email");
                    csvOutput.write("password");
                    csvOutput.write("approved");
                    csvOutput.write("type");
                    csvOutput.endRecord();
                    //update users file
                    for (User u: this.approvedUsers) {
                        csvOutput.write(u.getUsername());
                        csvOutput.write(u.getPassword());
                        csvOutput.write("true");
                        csvOutput.write(u.getClass().getSimpleName());
                        csvOutput.endRecord();
                    }
                    for (User u: this.pendingUsers) {
                        csvOutput.write(u.getUsername());
                        csvOutput.write(u.getPassword());
                        csvOutput.write("false");
                        csvOutput.write(u.getClass().getSimpleName());
                        csvOutput.endRecord();
                    }
                    csvOutput.close();

                }catch(Exception e){
                    e.printStackTrace();
                }
                return;
            case "data/managerData.csv":
                try{
                    CsvWriter csvOutput = new CsvWriter(new FileWriter(path, false), ',');
                    //email,password
                    csvOutput.write("email");
                    csvOutput.write("password");
                    csvOutput.endRecord();
                    //update managers file
                    for (Manager u: this.managers) {
                        csvOutput.write(u.getUsername());
                        csvOutput.write(u.getPassword());
                        csvOutput.endRecord();
                    }
                    csvOutput.close();

                }catch(Exception e){
                    e.printStackTrace();
                }
                return;
            case "data/lotData.csv":
                try{
                    CsvWriter csvOutput = new CsvWriter(new FileWriter(path, false), ',');
                    //name, enabled
                    csvOutput.write("name");
                    csvOutput.write("enabled");
                    csvOutput.endRecord();
                    //update parking lots file
                    for (ParkingLot u: this.lots) {
                        csvOutput.write(u.getLotName());
                        csvOutput.write(u.getEnabled() ? "true" : "false");
                        csvOutput.endRecord();
                    }
                    csvOutput.close();

                }catch(Exception e){
                    e.printStackTrace();
                }
                return;
            case "data/parkingSpaceData.csv":
                try{
                    CsvWriter csvOutput = new CsvWriter(new FileWriter(path, false), ',');
                    //lot it belongs to, index, state, user, car
                    csvOutput.write("lot");
                    csvOutput.write("index");
                    csvOutput.write("state");
                    csvOutput.write("user");
                    csvOutput.write("car");
                    csvOutput.endRecord();
                    //update spaces file
                    for (ParkingLot u: this.lots) {
                        for (ParkingSpace p: u.getSpaces()) {
                            csvOutput.write(u.getLotName());
                            csvOutput.write(String.valueOf(p.getIndex()));
                            csvOutput.write(p.getState().getClass().getSimpleName());
                            csvOutput.write(p.getCurrentUser());
                            csvOutput.write(p.getCurrentCar());
                            csvOutput.endRecord();
                        }
                    }
                    csvOutput.close();

                }catch(Exception e){
                    e.printStackTrace();
                }
                return;
        }
    }
}
