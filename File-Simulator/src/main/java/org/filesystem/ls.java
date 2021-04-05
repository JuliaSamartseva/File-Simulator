package org.filesystem;/*
 * $Id: ls.java,v 1.6 2001/10/12 02:14:31 rayo Exp $
 */

/*
 * $Log: ls.java,v $
 * Revision 1.6  2001/10/12 02:14:31  rayo
 * better formatting
 *
 * Revision 1.5  2001/10/07 23:48:55  rayo
 * added author javadoc tag
 *
 */

/**
 * A simple directory listing program for a simulated file system.
 * <p>
 * Usage:
 * <pre>
 *   java ls <i>path-name</i> ...
 * </pre>
 *
 * @author Ray Ontko
 */
public class ls {
  /**
   * The name of this program.
   * This is the program name that is used
   * when displaying error messages.
   */
  public static String PROGRAM_NAME = "ls";

  /**
   * Lists information about named files or directories.
   *
   * @throws Exception if an exception is thrown
   *                   by an underlying operation
   */
  public static void main(String[] args) throws Exception {
    // initialize the file system simulator kernel
    Kernel.initialize();
    String alignFormat = "| %-8d | %-8d | %-6d | %-3s (%-9s)   | %-12d | %-4d | %-13s | %n";


    // for each path-name given
    for (String name : args) {
      int status = 0;

      // stat the name to get information about the file or directory
      Stat stat = new Stat();
      status = Kernel.stat(name, stat);
      if (status < 0) {
        Kernel.perror(PROGRAM_NAME);
        Kernel.exit(1);
      }

      // mask the file type from the mode
      short type = (short) (stat.getMode() & Kernel.S_IFMT);

      // if name is a regular file, print the info
      if (type == Kernel.S_IFREG) {
        print(name, stat, alignFormat);
      }

      // if name is a directory open it and read the contents
      else if (type == Kernel.S_IFDIR) {
        // open the directory
        int fd = Kernel.open(name, Kernel.O_RDONLY);
        if (fd < 0) {
          Kernel.perror(PROGRAM_NAME);
          System.err.println(PROGRAM_NAME +
                  ": unable to open \"" + name + "\" for reading");
          Kernel.exit(1);
        }
        printHeader(name);
        // create a directory entry structure to hold data as we read
        DirectoryEntry directoryEntry = new DirectoryEntry();
        int count = 0;

        // while we can read, print the information on each entry
        while (true) {
          // read an entry; quit loop if error or nothing read
          status = Kernel.readdir(fd, directoryEntry);
          if (status <= 0)
            break;

          // get the name from the entry
          String entryName = directoryEntry.getName();

          // call stat() to get info about the file
          status = Kernel.stat(name + "/" + entryName, stat);
          if (status < 0) {
            Kernel.perror(PROGRAM_NAME);
            Kernel.exit(1);
          }

          // print the entry information
          print(entryName, stat, alignFormat);
          count++;
        }

        // check to see if our last read failed
        if (status < 0) {
          Kernel.perror("main");
          System.err.println("main: unable to read directory entry from /");
          Kernel.exit(2);
        }

        // close the directory
        Kernel.close(fd);
        printFooter(count);
      }
    }
    // exit with success if we process all the arguments
    Kernel.exit(0);
  }

  /**
   * Print a listing for a particular file.
   * This is a convenience method.
   *
   * @param name the name to print
   * @param stat the stat containing the file's information
   */
  private static void print(String name, Stat stat, String alignFormat) {
    short uid = stat.getUid();
    short gid = stat.getGid();

    short type = stat.getMode();
    String userPermissions = Integer.toOctalString((type & Kernel.S_IRWXU) >> 6);
    String groupPermissions = Integer.toOctalString((type & Kernel.S_IRWXG) >> 3);
    String otherPermissions = Integer.toOctalString(type & Kernel.S_IRWXO);

    String permissions = "";
    permissions += userPermissions;
    permissions += groupPermissions;
    permissions += otherPermissions;

    String rwxUserPermissions = rwxPermissions(userPermissions);
    String rwxGroupPermissions = rwxPermissions(groupPermissions);
    String rwxOtherPermissions = rwxPermissions(otherPermissions);

    String rwxPermissions = "";
    if (rwxUserPermissions != null &&
            rwxGroupPermissions != null &&
            rwxOtherPermissions != null) {
      rwxPermissions += rwxUserPermissions;
      rwxPermissions += rwxGroupPermissions;
      rwxPermissions += rwxOtherPermissions;
    }

    int inodeNumber = stat.getIno();
    int size = stat.getSize();
    int nLink = stat.getNlink();
    System.out.format(alignFormat, uid, gid, nLink, permissions, rwxPermissions, inodeNumber, size, name);
  }

  private static void printFooter(int filesNumber) {
    System.out.format("+----------------------------------------------------------------------------------------+%n");
    System.out.println("total files: " + filesNumber);
  }

  private static void printHeader(String directoryName) {
    System.out.println();
    System.out.println(directoryName + ":");

    System.out.format("+----------+----------+--------+-------------------+--------------+------+---------------+%n");
    System.out.format("| User id  | Group id | Nlink  | Permissions       | Inode number | Size |   File name   |%n");
    System.out.format("+----------+----------+--------+-------------------+--------------+------+---------------+%n");
  }

  private static String rwxPermissions(String octalString) {
    int octalNumber = Integer.parseInt(octalString);
    switch (octalNumber) {
      case 0:
        return "---";
      case 1:
        return "--x";
      case 2:
        return "-w-";
      case 3:
        return "-wx";
      case 4:
        return "r--";
      case 5:
        return "r-x";
      case 6:
        return "rw-";
      case 7:
        return "rwx";
      default:
        return null;
    }
  }
}

