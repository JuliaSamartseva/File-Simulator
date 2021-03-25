public class find {
    public static String PROGRAM_NAME = "find" ;
    public static void main(String[] args) throws Exception {
        Kernel.initialize();
        String name = args[0] ;
        findPath(name);
        // exit with success if we process all the arguments
        Kernel.exit( 0 ) ;
    }

    private static void findPath(String pathName) throws Exception {
        // stat the name to get information about the file or directory
        Stat stat = new Stat() ;
        checkStatus(pathName,stat);
        System.out.println( pathName ) ;
        // mask the file type from the mode
        short type = (short)( stat.getMode() & Kernel.S_IFMT ) ;
       // if name is a directory open it and read the contents
        if( type == Kernel.S_IFDIR ) {
            // open the directory
            int fd = Kernel.open( pathName , Kernel.O_RDONLY ) ;
            checkFD(fd,pathName);
            // create a directory entry structure to hold data as we read
            DirectoryEntry directoryEntry = new DirectoryEntry() ;
            // while we can read, print the information on each entry
            int status=0;
            while( true ) {
                status = Kernel.readdir( fd , directoryEntry ) ;
                if( status <= 0 )
                    break;
                // get the name from the entry
                String entryName = directoryEntry.getName() ;
                checkStatus(pathName + "/" + entryName , stat);
                if(!entryName.equals(".")&&!entryName.equals("..")) {
                    String nextPath = setNextPath(pathName,entryName);
                    findPath(nextPath);
                }
            }
            checkLastStatus(status);
            //close the directory
            Kernel.close(fd);
        }
    }

    private static int checkStatus(String pathName, Stat stat) throws Exception {
        int status = Kernel.stat( pathName , stat ) ;
        if( status < 0 )
        {
            Kernel.perror( PROGRAM_NAME ) ;
            Kernel.exit( 1 ) ;
        }
        return status;
    }

    private static void checkFD(int fd, String pathName) throws Exception {
        if( fd < 0 )
        {
            Kernel.perror( PROGRAM_NAME ) ;
            System.err.println( PROGRAM_NAME +
                    ": unable to open \"" + pathName + "\" for reading" ) ;
            Kernel.exit(1) ;
        }
    }

    private static void checkLastStatus(int status) throws Exception {
        if( status < 0 )
        {
            Kernel.perror( "main" ) ;
            System.err.println( "main: unable to read directory entry from /" ) ;
            Kernel.exit(2) ;
        }
    }

    private static String setNextPath(String pathName, String entryName){
        if(pathName.equals("/"))
            return (pathName+entryName);
        else
            return (pathName+"/"+entryName);
    }
}
