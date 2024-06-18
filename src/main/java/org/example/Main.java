package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Main {

  static volatile boolean check = true;
  static Set<Long[]> mainFilteredSet = new LinkedHashSet<>();
  static Set<Map<Long, Set<Long[]>>> result = new LinkedHashSet<>();
  static int maxAmountElementsOfLine = 0;
  static String pathToFile = "C:\\Users\\User\\Desktop\\lng-4.txt";
  static final String regex = "^(\"\\d*\")(;\"\\d*\")*$";
  static final Pattern pattern = Pattern.compile(regex);

  public static void main(String[] args) {
    if (args.length > 0) {
      pathToFile = args[0];
    }


    Set<Long[]> copyOfMainSet = readFile(pathToFile, pattern);
    if (!copyOfMainSet.isEmpty()) {
      findMaxAmount();
      Set<Map<Long, Set<Long[]>>> forTest = findMatches(copyOfMainSet, maxAmountElementsOfLine);
      print(forTest);
    } else {
      System.out.println("файл пуст или неправильный формат данных");
    }
    }

  public static Set<Long[]> readFile(String pathToFile, Pattern pattern) {
    Set<Long[]> set = new LinkedHashSet<>();
    try (Stream<String> lines = Files.lines(Paths.get(pathToFile))) {
      set = lines.filter(s -> {
            Matcher matcher = pattern.matcher(s);
            return matcher.matches();
          })
          .filter(s -> (s.length() > 2))
          .distinct()
          .map(s -> s.replace("\"\"", "0"))
          .map(s -> s.replace("\"", ""))
          .map(s -> Arrays.stream(s.split(";"))
              .map(Long::valueOf)
              .toArray(Long[]::new))
          .collect(Collectors.toSet());
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println("ошибка чтения файла или неверный путь");
    }

    if (!set.isEmpty()) {
      mainFilteredSet.addAll(set);
    }

    return new LinkedHashSet<>(mainFilteredSet);
  }

  private static void findMaxAmount() {
    for (Long[] x : mainFilteredSet) {
      if (maxAmountElementsOfLine < x.length)
        maxAmountElementsOfLine = x.length;
    }
  }

  public static Set<Map<Long, Set<Long[]>>> findMatches(Set<Long[]> copyOfMainSet, int maxElements) {
    int size = 1;
    Set<Map<Long, Set<Long[]>>> setOfMatchesPerColumn = new LinkedHashSet<>();
    while (maxElements >= size) {
      int finalSize = size;
      long[] thumb = copyOfMainSet.stream()
          .flatMapToLong(s -> LongStream.of(Arrays.stream(s)
              .skip(finalSize - 1)
              .findFirst()
              .orElse(0L)))
          .toArray();

      Set<Long> clear = new HashSet<>();
      Set<Long> copies = new HashSet<>();
      for (long l : thumb) {
        if (l != 0 && !clear.add(l)) {
          copies.add(l);
        }
      }

      Map<Long, Set<Long[]>> mapWithMatches = copyOfMainSet.stream()
          .filter(s -> Arrays.stream(s)
              .skip(finalSize - 1)
              .limit(1)
              .anyMatch(copies::contains))
          .collect(Collectors.groupingBy(
              s -> Arrays.stream(s)
                  .skip(finalSize - 1)
                  .limit(1)
                  .findFirst().orElse(0L),
              Collectors.toSet()
          ));

      if (!mapWithMatches.isEmpty()) {
        result.add(mapWithMatches);
        setOfMatchesPerColumn.add(mapWithMatches);
      }
      size++;
    }
    return setOfMatchesPerColumn;
  }

  public static void print(Set<Map<Long, Set<Long[]>>> setOfDuplicatesInMaps) {
    String newFileName = pathToFile.replace(".txt", "-result.txt");
    Path outputPath = Path.of(newFileName);
    if (!Files.exists(outputPath)) {
      try {
        Files.createFile(outputPath);
      } catch (IOException e) {
        System.out.println(e.getMessage());
        System.out.println("ошибка создания файла");
      }
    }

    try (FileOutputStream fos = new FileOutputStream(outputPath.toFile());
        PrintStream out = new PrintStream(fos)) {
      out.println(takeQuantityOfGroups());
      setOfDuplicatesInMaps.forEach(m -> {
        for (Map.Entry<Long, Set<Long[]>> longs : m.entrySet()) {
          out.printf("группа %d\n", longs.getKey());
          longs.getValue().forEach(s -> out.println(Arrays.toString(s)));
        }
      });
    } catch (FileNotFoundException e) {
      System.out.println(e.getMessage());
      System.out.println("файл не найден");
    } catch (IOException e) {
      System.out.println(e.getMessage());
      System.out.println("ошибка записи в файл");
    }
  }

  private static int takeQuantityOfGroups() {
    int quantityOfGroups = 0;
    for (Map<Long, Set<Long[]>> x : result) {
      quantityOfGroups += x.keySet().size();
    }
    return quantityOfGroups;
  }
}
