# Vessel Anomaly Detection


<p align="center">
  <img width="846" height="391" src="https://github.com/f-coda/VesselAnomalyDetection-IJGIS-/blob/master/routes.jpg" alt="Sublime's custom image"/>
</p>

All the modern surveillance systems take advantage of the Automatic Identification System (AIS), a compulsory tracking system for many types of vessels. Ships that carry AIS transponders on board transmit their position and status in order to alert nearby vessels and ground stations, but this information can well be used to identify events of interest and support decision making. The detection of anomalies (i.e. unexpected sailing behavior) in vessels’ trajectories is such an event, which is of utmost importance. Approaches for detecting such anomalies vary from extracting normality models to searching for individual cases, such as AIS switch-off or collision avoidance maneuvers. 

The current repository follows the former method; it employs sparse historic AIS data and polynomial interpolation in order to extract shipping lanes. It modifies the **DB-Scan clustering algorithm** in order to achieve more coherent trajectory clusters, which are then composed to create the shipping lanes. The proposed approach implements distributed processing on **Apache Spark** in order to improve processing speed and scalability and is evaluated using real-world AIS data collected from terrestrial AIS receivers. 

### Dependencies

- scala 2.11.8
- elki 0.7.5
- spark-core 2.4.4


## Cite Us

If you use the above code for your research, please cite our paper:

- [A distributed framework for extracting maritime traffic patterns](https://doi.org/10.1080/13658816.2020.1792914)
       
      @article{kontopoulos2021distributed,
      title={A distributed framework for extracting maritime traffic patterns},
      author={Kontopoulos, Ioannis and Varlamis, Iraklis and Tserpes, Konstantinos},
      journal={International Journal of Geographical Information Science},
      volume={35},
      number={4},
      pages={767--792},
      year={2021},
      publisher={Taylor \& Francis}
      }

