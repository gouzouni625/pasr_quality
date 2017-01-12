package org.gouzouni625.pasr.quality.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gouzouni625.pasr.quality.model.DataSet;
import org.gouzouni625.pasr.quality.model.Partition;

import java.util.Random;


@NoArgsConstructor
@Getter
public class DataSetPartitioner {

    public void partition (DataSet dataSet, Partition partition) {
        int numberOfTrainingSamples = (int)(dataSet.size() * partition.getTrain());

        trainingDataSet = new DataSet();
        testingDataSet = new DataSet();

        Random random = new Random(System.currentTimeMillis());
        for(int i = 0;i < numberOfTrainingSamples;i++) {
            int chosen = random.nextInt(dataSet.size());

            trainingDataSet.add(dataSet.remove(chosen));
        }

        dataSet.forEach(testingDataSet :: add);
    }

    private DataSet trainingDataSet;
    private DataSet testingDataSet;

}
