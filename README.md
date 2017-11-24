# README #

A simple machine learning web server that caters for small datasets

# Usage

git clone this project and run the following command:

```bash
cd devops/build
./make.sh
```

If you are on Windows run the following command instead:

```bash
cd devops/build
./make.ps1
```

This will build the ml-webapi-jersey.jar and ml-webapi-spring.jar in the devops/bin folder

To run the web server, use the following command:

```bash
java -jar devops/bin/ml-webapi-spring.jar
```

Now navigate to your web browser and enter the following url: 

```html
http://localhost:8900
```

# Features

### Machine Learning ###

* ML-SVM: SVC, SVR, one-class SVC (nu, epsilon, primal, SMO)
* ML-Trees: Isolation Forest, ID3
* ML-kNN: kNN classifier, anomaly detection (D-Distance, kth-NN-Distance, Avg-kNN-Distance, ORCA)
* ML-GLM: generalized linear model, linear model, logistic model
* ML-GaussianMixture: multivariate normal outliers, RANSAC
* ML-LM: linear model
* ML-Logistic: logistic model
* ML-LOF: local outlier factors (LOF), cluster-based local outlier factors (CBLOF), local distance-based outlier factors (LDOF), local correlation integral (LOCI)
* ML-Clustering: k-means clustering, expectation maximization clustering, one-link clustering, DBSCAN
* ML-Bayes: naive bayes classifier
* ML-DataPreparation: input discretization using k-means, input and output normalization
* ML-ANN: kohonen SOFM, MLP neural network, Adaptive Resonance Theory (ART1, Fuzzy ART, ARTMAP)
* ML-ARM: Apriori association rule mining
* ML-Reinforcement: Q-Learn, Q-Learn(lambda), SARSA, SARSA(lambda), action selection policy (SoftMax, EpsilonGreedy, Greedy, GibbsSoftMax)
* ML-FALCON: R-FALCON, TD-FALCON (Q-FALCON, S-FALCON, Q-FALCON(lambda), S-FALCON(lambda), applications (TSP, MineField, Flocking, Traffic)
* SK-Statistics: ICA (KICA or Kurtois Maximization ICA), PCA, Dynamic Time Warping (DTW)
* ML-NLP: Natural Language Processing 
 
### Search and Optimisation ###
* Search: nonlinear conjugate gradient search 

### Anomaly Detection ###
* one-class SVC (unsupervised)
* Gaussian Mixture (unsupervised)
* ORCA (unsupervised)
* Isolation Forest (unsupervised)
* Distance based (D-Distance, kth-NN-Distance, Avg-kNN-Distance) (unsupervised)
* LOF (unsupervised)
* CBLOF (unsupervised)
* LDOF (unsupervised)
* LOCI (unsupervised)
* RANSAC (unsupervised)
* logistic binary classifier (supervised)
* SVC (supervised)
* kNN (supervised)

### Time Series Prediction ###
* Regression: SVR, GLM, LM, MLP
* Classification: kNN, SVC, Logistic, MLP, ARTMAP, ID3, C45
* Clustering: k-means, EM clustering, kohonen, Fuzzy ART, DBSCAN, single-link/hierarchical clustering
* Dynamic Time Warping (time series analysis: measure difference between two time series)
* Reinforcement Learning (Q-LEARN, FALCON ...)

### Log Event Analytics ###
* ARM: Apriori rule mining
* Classification: kNN, NBC, SVC, Logistic or GLM, MLP, ARTMAP, ID3, C45
* Clustering: k-means, EM clustering, kohonen, single-link, ART1, Fuzzy ART, DBSCAN, hierarchical clustering

### Spark Algorithm ###
* Classifiers: NBC
* Clustering: K-Means
* Anomaly Detection: Isolation Forest
* Recommender: Connection Recommendation, CWBTIAB, Content Filtering
* ARM: Apriori

### Text Mining ###
* Topic Modeling: pLSA, Latent Semantic Allocation (LSA)
* Text Classification: NBC
* Text Clustering: K-Means

### Natural Language Processing ###
* POS Tagging

### Text Retrieval ###
* Filter: Porter Stemmer, Stop Word Removal, Lower Casing
* Tokenizer: English Tokenizer

### Input Processing ###
* input transform: allow user to add, remove, numerically modify a particular attribute in the dataset before passing into algorithms
* input discretization: convert input numerical attributes to categorical attributes
* input level conversion: convert input categorical attributes to numerical attributes
* source separation: ICA which separate data sources from a mix signal (e.g., cocktail party problem)
* feature dimension reduction and restore: PCA which reduce the dimension of high-dimension data for processing by machine learning
* normalization: normalize data for machine learning preprocessing

### Model Tuning ###
* parameter tuning: automatically tune the parameters of ML algorithm using parameter sweeping, random, or optimization search
* model selection: automatically select attributes from the data to be used in the model of the ML algorithms
* sample size selection: automatically select the training sample size

### Unit Testing ###
* binary classifiers: supervised learning for two-class based classification 
* anomaly: anomaly detection either using supervised or unsupervised approach
* classifiers: multi-label supervised classification
* regressions: regression-based forecasting
* clustering: one-link clustering, EM clustering, k-means clustering

### Web Service ###
* ML-Jersey (Async RESTful services with jersey)
* RESTful services: project CRUD, algorithm CRUD, data CRUD, training and prediction
* ML-Tomcat (Java Servlet Container option)
* ML-Grizzly (HTTP Container option)
* ML-SpringText: rest services implemented using RESTController in spring

### Web Client ###
* AngularJS portal for CRUD on project, ml modules and data batches
* allow user to train and run classifiers/anomaly detection/clustering/... using the web application via web browser
* flow chart to connect modules and batch for machine learning workflow construction
* experiment for comparing performance of various algorithms

### Others ###
ML-MATSim: Agent-based Transport Simulation


