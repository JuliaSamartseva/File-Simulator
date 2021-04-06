package org.filesystem;

public class Link {

  public static String PROGRAM_NAME = "link";

  public static void main(String[] argv) throws Exception {
    Kernel.initialize();

    if (argv.length != 2) {
      System.err.println(
              PROGRAM_NAME + " usage: java link <existing filepath> <new link filepath>");
      Kernel.exit(1);
    }

    String existingFilepath = argv[0];
    String linkFilepath = argv[1];

    Kernel.link(existingFilepath, linkFilepath);
  }
}
