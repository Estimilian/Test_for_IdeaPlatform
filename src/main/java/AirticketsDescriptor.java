
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

//Условие:
//Напишите программу на языке программирования java, которая прочитает файл tickets.json и рассчитает:
//- среднее время полета между городами Владивосток и Тель-Авив
//- 90-й процентиль времени полета между городами Владивосток и Тель-Авив
//Программа должна вызываться из командной строки Linux, результаты должны быть представлены в текстовом виде.
//В качестве результата нужно прислать ответы на поставленные вопросы и ссылку на исходный код.

public class AirticketsDescriptor {

    public static void main(String[] args) {

        JSONParser jsonParser = new JSONParser();
        JSONArray tickets;
        HashMap ticket;
        ZonedDateTime departure, arrival;
        LocalDate dateBuffer;
        LocalTime timeBuffer;
        ZoneId zoneV = ZoneId.of("Asia/Vladivostok");
        ZoneId zoneT = ZoneId.of("Asia/Tel_Aviv");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy");
        String origin, destination;
        int numberOfElement = 0; // номер текущего элемента массива или размер массива когда мы его обработали
        ArrayList<Long> flyTime = new ArrayList(10); //массив длительностей полетов

        //считывание и приведени JSON в объекты java
        try (FileReader reader = new FileReader("tickets.json")) {
            //У объекта JSON всего одно поле - массив, нет смысла создавать отдельную переменную под объект
            tickets = (JSONArray) ((JSONObject) jsonParser.parse(reader)).get("tickets");
            while (true) {
                try { //выходим из цикла как только пройдем весь массив
                    ticket = (HashMap) tickets.get(numberOfElement);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
                origin = (String) ticket.get("origin");
                destination = (String) ticket.get("destination");
                if (origin.equals("VVO")&&destination.equals("TLV")) {
                    dateBuffer = LocalDate.parse((CharSequence) ticket.get("departure_date"), dtf);
                    try { //иногда значение времени выглядит как 9:00, а не 09:00, учитываем этот вариант
                        timeBuffer = LocalTime.parse((CharSequence) ticket.get("departure_time"));
                    } catch (DateTimeParseException dte) {
                        timeBuffer = LocalTime.parse("0" + ticket.get("departure_time"));
                    }
                    departure = ZonedDateTime.of(dateBuffer, timeBuffer, zoneV);
                    dateBuffer = LocalDate.parse((CharSequence) ticket.get("arrival_date"), dtf);
                    try { //иногда значение времени выглядит как 9:00, а не 09:00, учитываем этот вариант
                        timeBuffer = LocalTime.parse((CharSequence) ticket.get("arrival_time"));
                    } catch (DateTimeParseException dte) {
                        timeBuffer = LocalTime.parse("0" + ticket.get("arrival_time"));
                    }
                    arrival = ZonedDateTime.of(dateBuffer, timeBuffer, zoneT);
                    flyTime.add(ChronoUnit.MINUTES.between(departure, arrival));
                    numberOfElement++;
                }
            }
        } catch (final Exception ex) {
                ex.printStackTrace();
                System.out.println("something went wrong while JSON reading. Try to check file source");
                }

        //ищем среднее время полета
        long averageTime = 0;
        for (int a = 0; a < numberOfElement; a++) {
            averageTime += flyTime.get(a);
        }
        averageTime = Math.round(averageTime * 1.0 / 10);
        System.out.println("Среднее время полета среди всех доступных предложений "
                + averageTime / 60 + " часов, " + averageTime % 60 + " минут");

        //ищем, какой элемент в упорядоченном массиве будет 90-м процентилем
        long prcentil90Time = flyTime.get((int) (numberOfElement - Math.round(0.9 * (numberOfElement +1))));
        Collections.sort(flyTime);
        System.out.println("Полеты длительностью " + prcentil90Time / 60 + " часов, " + prcentil90Time % 60 + " минут"
                                + " и менее - быстрее 90% предложений между городами Владивосток и Тель-Авив.");
    }
}




