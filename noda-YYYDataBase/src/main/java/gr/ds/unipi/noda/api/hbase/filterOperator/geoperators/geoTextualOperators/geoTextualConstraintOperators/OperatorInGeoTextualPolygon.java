package gr.ds.unipi.noda.api.hbase.filterOperator.geoperators.geoTextualOperators.geoTextualConstraintOperators;

import gr.ds.unipi.noda.api.core.operators.filterOperators.geoperators.geometries.Polygon;
import gr.ds.unipi.noda.api.core.operators.filterOperators.textualOperators.conditionalTextualOperators.ConditionalTextualOperator;
import gr.ds.unipi.noda.api.hbase.filterOperator.geoperators.geographicalOperators.OperatorInGeoPolygon;

public class OperatorInGeoTextualPolygon extends GeoTextualConstraintOperator<Polygon> {
    protected OperatorInGeoTextualPolygon(String fieldName, Polygon polygon, ConditionalTextualOperator conditionalTextualOperator) {
        super(OperatorInGeoPolygon.newOperatorInGeoPolygon(fieldName, polygon), conditionalTextualOperator);
    }

    @Override
    public Object getOperatorExpression() {
        return null;
    }

    public static OperatorInGeoTextualPolygon inGeoTextualPolygon(String fieldName, Polygon polygon, ConditionalTextualOperator conditionalTextualOperator){
        return new OperatorInGeoTextualPolygon(fieldName, polygon, conditionalTextualOperator);
    }
}
