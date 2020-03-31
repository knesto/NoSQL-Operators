package gr.ds.unipi.noda.api.client;

import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static gr.ds.unipi.noda.api.core.operators.AggregateOperators.count;
import static gr.ds.unipi.noda.api.core.operators.AggregateOperators.max;
import static gr.ds.unipi.noda.api.core.operators.FilterOperators.*;
import static gr.ds.unipi.noda.api.core.operators.SortOperators.asc;

public class NoSqlDbSystemTest {

    @Test
    public void neo4j() {
        NoSqlDbSystem noSqlDbSystem = NoSqlDbSystem.Neo4j().username("neo4j").password("nikos").host("localhost").port(7687).database("graph").build();
        noSqlDbSystem.operateOn("Ship").filter(eq("LAT","'-38.31416'")).printScreen();
        noSqlDbSystem.closeConnection();

        NoSqlDbSys.MongoDB().Builder("", "", "").build().;

    }

    @Test
    public void groupingAndsortingExample() {
        NoSqlDbSystem noSqlDbSystem = NoSqlDbSystem.Neo4j().username("neo4j").password("nikos").host("localhost").port(7687).database("graph").build();
        noSqlDbSystem.operateOn("Ship").filter(eq("LAT","'-38.31416'")).groupBy("fieldA", max("fieldB")).sort(asc("fieldC")).printScreen();
        noSqlDbSystem.closeConnection();
    }

    @Test
    public void countExample() {
        NoSqlDbSystem noSqlDbSystem = NoSqlDbSystem.Neo4j().username("neo4j").password("nikos").host("localhost").port(7687).database("graph").build();
        noSqlDbSystem.operateOn("Ship").filter(eq("LAT","'-38.31416'")).groupBy("fieldA", count()).printScreen();
        noSqlDbSystem.operateOn("Ship").filter(eq("LAT","'-38.31416'")).count();

        noSqlDbSystem.closeConnection();
    }

    @Test
    public void check(){
        String[] c = new String[998];
        for(int i =0;i<998;i++){
            c[i] = "sdfsgfmjnijni";
        }

        long t1 = System.currentTimeMillis();
        something1("adfsdf","sfdgdfg",c);
        System.out.println(System.currentTimeMillis() - t1 +"ms");


        long t2 = System.currentTimeMillis();
        something2("adfsdf","sfdgdfg",c);
        System.out.println(System.currentTimeMillis() - t2 +"ms");

    }

    public static void something1(String a, String b, String[] c){
        String[] d = new String[1000];
        d[0] = a;
        d[1] = b;
        for(int i =2;i<1000;i++){
            d[i] = c[i-2];
        }
    }

    public static void something2(String a, String b, String[] c){
        Arrays.stream(new String[]{a,b} ).toArray(String[]::new);
    }
}