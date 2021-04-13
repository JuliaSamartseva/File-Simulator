package org.filesystem;

import java.io.IOException;
import java.util.HashSet;

public class Fsck {
  public static String PROGRAM_NAME = "fsck";

  public static void main(String[] argv) throws Exception {
    Kernel.initialize();
    FileSystem rootFileSystem = Kernel.getRootFileSystem();

    HashSet<Integer> allocatedDataBlocks = getAllocatedDataBlocks(rootFileSystem);
    int errors = checkFreeListBits(rootFileSystem, allocatedDataBlocks);

    if (errors == 0) {
      System.out.println(
              PROGRAM_NAME + " check finished with success, no errors found");
    } else {
      System.out.println(
              PROGRAM_NAME + " check finished with failure, " + errors + " errors found");
    }

    Kernel.exit(0);
  }

  private static HashSet<Integer> getAllocatedDataBlocks(FileSystem fileSystem) throws Exception {
    int indexNodeNumber = getIndexNodeNumber(fileSystem);
    HashSet<Integer> allocatedDataBlocks = new HashSet<>();
    for (int i = 0; i < indexNodeNumber; i++) {
      IndexNode indexNode = new IndexNode();
      fileSystem.readIndexNode(indexNode, (short) i);
      if (indexNode.getMode() != 0) {
        for (int j = 0; j < IndexNode.MAX_DIRECT_BLOCKS; j++) {
          if (indexNode.getDirectIndexBlock(j) != FileSystem.NOT_A_BLOCK) {
            int address = indexNode.getBlockAddress(j);
            allocatedDataBlocks.add(address);
          }
        }
      }
    }
    return allocatedDataBlocks;
  }

  private static int checkFreeListBits(FileSystem fileSystem, HashSet<Integer> allocatedDataBlocks) throws IOException {
    int freeListBitsSize = fileSystem.getFreeListBitsSize();
    int errors = 0;
    for (int i = 0; i < freeListBitsSize; i++) {
      if (allocatedDataBlocks.contains(i)) {
        if (!fileSystem.isBitBlockSet(i)) {
          System.err.println(
                  PROGRAM_NAME + " block mentioned in the inode is marked as a free block");
          errors++;
        }
      } else {
        if (fileSystem.isBitBlockSet(i)) {
          System.err.println(
                  PROGRAM_NAME + " block not mentioned in the inode is marked as an allocated block");
          errors++;
        }
      }
    }

    return errors;
  }

  private static int getIndexNodeNumber(FileSystem fileSystem) {
    return fileSystem.getDataBlockOffset() - fileSystem.getInodeBlockOffset();
  }
}
