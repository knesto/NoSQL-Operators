package gr.ds.unipi.noda.api.neo4j.filterOperators.geoperators.geoTemporalOperators;


import gr.ds.unipi.noda.api.core.operators.filterOperators.geoperators.geoTemporalOperators.temporal.TemporalBounds;
import gr.ds.unipi.noda.api.core.operators.filterOperators.geoperators.geometries.Rectangle;
import gr.ds.unipi.noda.api.neo4j.filterOperators.geoperators.geographicalOperators.OperatorInGeographicalRectangle;
import org.davidmoten.hilbert.HilbertCurve;
import org.davidmoten.hilbert.Ranges;
import org.davidmoten.hilbert.SmallHilbertCurve;

final class OperatorInGeoTemporalRectangle extends GeoTemporalOperator<Rectangle, TemporalBounds> {

    protected OperatorInGeoTemporalRectangle(String fieldName, Rectangle rectangle, String temporalFieldName, TemporalBounds temporalType) {
        super(OperatorInGeographicalRectangle.newOperatorInGeographicalRectangle(fieldName, rectangle), temporalFieldName, temporalType);
    }

    public static OperatorInGeoTemporalRectangle newOperatorInGeoTemporalRectangle(String fieldName, Rectangle rectangle, String temporalFieldName, TemporalBounds temporalType) {
        return new OperatorInGeoTemporalRectangle(fieldName, rectangle, temporalFieldName, temporalType);
    }

    @Override
    public StringBuilder getOperatorExpression() {
        StringBuilder sb = new StringBuilder();

        int bits = 8;

        long maxOrdinates = 1L << bits;

        SmallHilbertCurve f = HilbertCurve.small().bits(bits).dimensions(3);


        long[] point1 = scalePoint(getGeographicalOperator().getGeometry().getMbr().getLowerBound().getLatitude(), getGeographicalOperator().getGeometry().getMbr().getLowerBound().getLongitude(), getTemporalType().getLowerBound().getTime(), 1546992000, 1554854399, maxOrdinates);
        long[] point2 = scalePoint(getGeographicalOperator().getGeometry().getMbr().getUpperBound().getLatitude(), getGeographicalOperator().getGeometry().getMbr().getUpperBound().getLongitude(), getTemporalType().getUpperBound().getTime(), 1546992000, 1554854399, maxOrdinates);
//// return just one range
        System.out.println("POINT 1 = " + point1);
        System.out.println("POINT 2 = " + point2);
        int maxRanges = 1;
        Ranges ranges = f.query(point1, point2, maxRanges);
        System.out.println(ranges);
        ranges.forEach(range -> {
            long low = range.low();
            long high = range.high();
            System.out.println(range.low());
            System.out.println(range.high());

            if(low != high) {
                sb.append("s.STHilbertIndex > " + low + " AND s.STHilbertIndex < " + high + " WITH s WHERE point({ srid:7203 , x: " + getGeographicalOperator().getGeometry().getLowerBound().getLatitude() + ", y: "+ getGeographicalOperator().getGeometry().getLowerBound().getLongitude() +" }) < s." + getGeographicalOperator().getFieldName() + " < point({ srid: 7203 , x: " + getGeographicalOperator().getGeometry().getUpperBound().getLatitude() + ", y: "+ getGeographicalOperator().getGeometry().getUpperBound().getLongitude() + " }) AND " + getTemporalType().getLowerBound().getTime() + " < s." + getTemporalFieldName()  + " < " + getTemporalType().getUpperBound().getTime() );

            } else {
                sb.append("s.STHilbertIndex = " + low + " WITH s WHERE point({ srid:7203 , x: " + getGeographicalOperator().getGeometry().getLowerBound().getLatitude() + ", y: "+ getGeographicalOperator().getGeometry().getLowerBound().getLongitude() +" }) < s." + getGeographicalOperator().getFieldName() + " < point({ srid: 7203 , x: " + getGeographicalOperator().getGeometry().getUpperBound().getLatitude() + ", y: "+ getGeographicalOperator().getGeometry().getUpperBound().getLongitude() + " }) AND " + getTemporalType().getLowerBound().getTime() + " < s." + getTemporalFieldName()  + " < " + getTemporalType().getUpperBound().getTime() );

            }

        });

        return sb;

    }

}
