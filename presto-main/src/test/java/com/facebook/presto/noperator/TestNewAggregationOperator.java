package com.facebook.presto.noperator;

import com.facebook.presto.operator.Page;
import com.facebook.presto.sql.planner.plan.AggregationNode.Step;
import com.facebook.presto.sql.tree.Input;
import com.facebook.presto.util.MaterializedResult;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.List;

import static com.facebook.presto.block.BlockAssertions.COMPOSITE_SEQUENCE_TUPLE_INFO;
import static com.facebook.presto.noperator.NewOperatorAssertion.assertOperatorEquals;
import static com.facebook.presto.noperator.RowPagesBuilder.rowPagesBuilder;
import static com.facebook.presto.operator.AggregationFunctionDefinition.aggregation;
import static com.facebook.presto.operator.aggregation.CountAggregation.COUNT;
import static com.facebook.presto.operator.aggregation.CountColumnAggregation.COUNT_COLUMN;
import static com.facebook.presto.operator.aggregation.DoubleSumAggregation.DOUBLE_SUM;
import static com.facebook.presto.operator.aggregation.LongAverageAggregation.LONG_AVERAGE;
import static com.facebook.presto.operator.aggregation.LongSumAggregation.LONG_SUM;
import static com.facebook.presto.operator.aggregation.VarBinaryMaxAggregation.VAR_BINARY_MAX;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_LONG;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_VARBINARY;
import static com.facebook.presto.tuple.TupleInfo.Type.DOUBLE;
import static com.facebook.presto.tuple.TupleInfo.Type.FIXED_INT_64;
import static com.facebook.presto.tuple.TupleInfo.Type.VARIABLE_BINARY;
import static com.facebook.presto.util.MaterializedResult.resultBuilder;

public class TestNewAggregationOperator
{
    @Test
    public void testAggregation()
            throws Exception
    {
        List<Page> input = rowPagesBuilder(SINGLE_VARBINARY, SINGLE_LONG, SINGLE_VARBINARY, COMPOSITE_SEQUENCE_TUPLE_INFO)
                .addSequencePage(100, 0, 0, 300, 500)
                .build();

        NewAggregationOperator operator = new NewAggregationOperator(
                Step.SINGLE,
                ImmutableList.of(aggregation(COUNT, new Input(0, 0)),
                        aggregation(LONG_SUM, new Input(1, 0)),
                        aggregation(LONG_AVERAGE, new Input(1, 0)),
                        aggregation(VAR_BINARY_MAX, new Input(2, 0)),
                        aggregation(COUNT_COLUMN, new Input(0, 0)),
                        aggregation(LONG_SUM, new Input(3, 1)),
                        aggregation(DOUBLE_SUM, new Input(3, 2)),
                        aggregation(VAR_BINARY_MAX, new Input(3, 3))));

        MaterializedResult expected = resultBuilder(FIXED_INT_64, FIXED_INT_64, DOUBLE, VARIABLE_BINARY, FIXED_INT_64, FIXED_INT_64, DOUBLE, VARIABLE_BINARY)
                .row(100, 4950, 49.5, "399", 100, 54950, 54950.0, "599")
                .build();

        assertOperatorEquals(operator, input, expected);
    }
}