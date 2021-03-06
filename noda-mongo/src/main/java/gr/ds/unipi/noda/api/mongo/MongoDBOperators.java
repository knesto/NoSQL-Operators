package gr.ds.unipi.noda.api.mongo;

import com.mongodb.client.MongoCursor;
import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.config.ReadConfig;
import gr.ds.unipi.noda.api.core.nosqldb.NoSqlDbConnector;
import gr.ds.unipi.noda.api.core.nosqldb.NoSqlDbOperators;
import gr.ds.unipi.noda.api.core.operators.aggregateOperators.AggregateOperator;
import gr.ds.unipi.noda.api.core.operators.filterOperators.FilterOperator;
import gr.ds.unipi.noda.api.core.operators.sortOperators.SortOperator;
import gr.ds.unipi.noda.api.mongo.filterOperators.geoperators.geographicalOperators.MongoDBGeographicalOperatorFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;
import org.bson.conversions.Bson;
import scala.collection.JavaConversions;

import java.util.*;

final class MongoDBOperators extends NoSqlDbOperators {

    private final MongoDBConnectionManager mongoDBConnectionManager = MongoDBConnectionManager.getInstance();
    private final List<Bson> stagesList;
    private final String database;
    private final String uriSparkSession;

    private final List<Map.Entry<List<String>,List<AggregateOperator>>> groupByAgg = new ArrayList<>();

    private MongoDBOperators(NoSqlDbConnector connector, String s, SparkSession sparkSession) {
        super(connector, s, sparkSession);
        stagesList = new ArrayList<>();

        MongoDBConnector mongoDBConnector = ((MongoDBConnector) connector);
        database = mongoDBConnector.getDatabase();
        uriSparkSession = mongoDBConnector.getMongoURIForSparkSession();
    }

    static MongoDBOperators newMongoDBOperators(NoSqlDbConnector connector, String s, SparkSession sparkSession) {
        return new MongoDBOperators(connector, s, sparkSession);
    }

    @Override
    public NoSqlDbOperators filter(FilterOperator filterOperator, FilterOperator... filterOperators) {

        if (MongoDBGeographicalOperatorFactory.isOperatorGeoNearestNeighbor(filterOperator)) {
            stagesList.add(Document.parse(filterOperator.getOperatorExpression().toString()));
        } else {
            stagesList.add(Document.parse(" { $match: " + filterOperator.getOperatorExpression() + " } "));
        }

        for (FilterOperator fops : filterOperators) {
            if (MongoDBGeographicalOperatorFactory.isOperatorGeoNearestNeighbor(fops)) {
                stagesList.add(Document.parse(fops.getOperatorExpression().toString()));
            } else {
                stagesList.add(Document.parse(" { $match: " + fops.getOperatorExpression() + " } "));
            }
        }
        return this;
    }

    @Override
    public int count() {
        stagesList.add(Document.parse("{ $count: \"count\" }"));
        MongoCursor mc = mongoDBConnectionManager.getConnection(getNoSqlDbConnector()).getDatabase(database).getCollection(getDataCollection()).aggregate(stagesList).iterator();

        if (mc.hasNext()) {
            return ((Document) mc.next()).getInteger("count", -10);
        }

        return 0;
    }

    @Override
    public NoSqlDbOperators sort(SortOperator sortOperator, SortOperator... sortingOperators) {

        StringBuilder sb = new StringBuilder();

        sb.append("{ $sort : ");
        sb.append("{ ");

        sb.append(sortOperator.getOperatorExpression());

        for (SortOperator so : sortingOperators) {
            sb.append(", ");
            sb.append(so.getOperatorExpression());
        }

        sb.append(" } }");

        stagesList.add(Document.parse(sb.toString()));

        return this;
    }

    @Override
    public NoSqlDbOperators limit(int limit) {
        stagesList.add(Document.parse("{ $limit: " + limit + " }"));
        return this;
    }

    @Override
    public Optional<Double> max(String fieldName) {
        stagesList.add(Document.parse("{ $group: { _id:null, " + AggregateOperator.aggregateOperator.newOperatorMax(fieldName).getOperatorExpression() + " } }"));
        MongoCursor mc = mongoDBConnectionManager.getConnection(getNoSqlDbConnector()).getDatabase(database).getCollection(getDataCollection()).aggregate(stagesList).iterator();
        if (mc.hasNext()) {
            return Optional.of(((Document) mc.next()).getDouble("max_" + fieldName));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> min(String fieldName) {
        stagesList.add(Document.parse("{ $group: { _id:null, " + AggregateOperator.aggregateOperator.newOperatorMin(fieldName).getOperatorExpression() + " } }"));
        MongoCursor mc = mongoDBConnectionManager.getConnection(getNoSqlDbConnector()).getDatabase(database).getCollection(getDataCollection()).aggregate(stagesList).iterator();
        if (mc.hasNext()) {
            return Optional.of(((Document) mc.next()).getDouble("min_" + fieldName));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> sum(String fieldName) {
        stagesList.add(Document.parse("{ $group: { _id:null, " + AggregateOperator.aggregateOperator.newOperatorSum(fieldName).getOperatorExpression() + " } }"));
        MongoCursor mc = mongoDBConnectionManager.getConnection(getNoSqlDbConnector()).getDatabase(database).getCollection(getDataCollection()).aggregate(stagesList).iterator();
        if (mc.hasNext()) {
            return Optional.of(((Document) mc.next()).getDouble("sum_" + fieldName));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> avg(String fieldName) {
        stagesList.add(Document.parse("{ $group: { _id:null, " + AggregateOperator.aggregateOperator.newOperatorAvg(fieldName).getOperatorExpression() + " } }"));
        MongoCursor mc = mongoDBConnectionManager.getConnection(getNoSqlDbConnector()).getDatabase(database).getCollection(getDataCollection()).aggregate(stagesList).iterator();
        if (mc.hasNext()) {
            return Optional.of(((Document) mc.next()).getDouble("avg_" + fieldName));
        }
        return Optional.empty();
    }

    @Override
    public NoSqlDbOperators groupBy(String fieldName, String... fieldNames) {

        StringBuilder sb = new StringBuilder();

        sb.append("{ $group: ");
        sb.append("{ _id: {");

        sb.append(fieldName+": " + "\"" + "$" + fieldName + "\"");

        if(fieldNames.length != 0){
            for(String fn : fieldNames){
                sb.append(",");
                sb.append(fieldName+": " + "\"" + "$" + fieldName + "\"");
            }
        }

        sb.append("}");

        sb.append(" } }");

        stagesList.add(Document.parse(sb.toString()));

        return this;
    }

    @Override
    public NoSqlDbOperators aggregate(AggregateOperator aggregateOperator, AggregateOperator... aggregateOperators) {

        if(stagesList.size() > 0 && ((Document) stagesList.get(stagesList.size()-1)).containsKey("$group")){

            StringBuilder sb = new StringBuilder();

            Document document = (Document) stagesList.get(stagesList.size()-1);
            String json = document.toJson();
            sb.append(json, 0, json.length()-3);

            sb.append(aggregateOperator.getOperatorExpression());

            if(aggregateOperators.length != 0){
                for(AggregateOperator aop : aggregateOperators){
                    sb.append(", "+aop.getOperatorExpression());
                }
            }

            sb.append(" } }");
            stagesList.add(Document.parse(sb.toString()));

        }else{
            StringBuilder sb = new StringBuilder();
            sb.append("{ $group: ");

            sb.append("{ _id: null ");

            sb.append(", " + aggregateOperator.getOperatorExpression());

            if (aggregateOperators.length != 0) {
                for (AggregateOperator aop : aggregateOperators) {
                    sb.append(", " + aop.getOperatorExpression());
                }
            }

            sb.append(" } }");
            stagesList.add(Document.parse(sb.toString()));
        }

        return this;
    }

    @Override
    public NoSqlDbOperators distinct(String fieldName) {
        return groupBy(fieldName);
    }

    @Override
    public void printScreen() {
        MongoCursor<Document> cursor = mongoDBConnectionManager.getConnection(getNoSqlDbConnector()).getDatabase(database).getCollection(getDataCollection()).aggregate(stagesList).iterator();
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public NoSqlDbOperators project(String fieldName, String... fieldNames) {

        StringBuilder sb = new StringBuilder();
        sb.append("{ $project : { ");

        sb.append("\"_id\" : 0, ");

        sb.append("\""+fieldName + "\""+ " : 1");

        for (String s : fieldNames) {
            sb.append(", ");
            sb.append("\"" + s + "\"" + " : 1");
        }

        sb.append(" } }");

        stagesList.add(Document.parse(sb.toString()));
        return this;

    }

    @Override
    public Dataset<Row> toDataframe() {

        Map<String, String> readOverrides = new HashMap<>();
        readOverrides.put("spark.mongodb.input.uri", uriSparkSession);
        readOverrides.put("spark.mongodb.input.database", database);
        readOverrides.put("spark.mongodb.input.collection", getDataCollection());

        ReadConfig readConfig = ReadConfig.create(getSparkSession()).withOptions(readOverrides).withPipeline(JavaConversions.asScalaBuffer(Collections.unmodifiableList(stagesList)).toSeq());

        return MongoSpark.loadAndInferSchema(getSparkSession(), readConfig);

    }

}
