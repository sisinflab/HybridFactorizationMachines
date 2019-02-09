# HybridFactorizationMachines
## Semantically Interpretable Factorization Machines

In the last decade, model-based recommender systems have shown their highly competitive performance in different domains and settings. In fact, they usually rely on the computation of latent factors to recommend items with a very high level of accuracy. Unfortunately, when moving to a latent space it is hard to keep references to the actual semantics of the recommended item, thus making the predictive model a black-box oracle.
In this work, we show how to exploit semantic features coming from knowledge graphs to properly initialize latent factors in Factorization Machines, thus training an interpretable model. In the presented approach, semantic features are injected into the learning process to retain the original informativeness of the items available in the catalog. An experimental evaluation on three different datasets shows the effectiveness of the obtained interpretable model in terms both of accuracy and diversity for recommendation results.

In this work, we propose a _knwowledge-aware Hybrid Factorization Machine_ (_kaHFM_) to train interpretable models in recommendation scenarios. _kaHFM_ relies on Factorization Machines and it extends them in different key aspects making use of the semantic information encoded in a knowledge graph.

With _kaHFM_ we build a model in which the meaning of each latent factor is bound to an explicit content-based feature extracted from a knowledge graph. Doing this, after the model has been trained, we still have an explicit reference to the original semantics of the features describing the items, thus making possible the interpretation of the final results. 

## Reference
If you publish research that uses _kaHFM_ please use:
~~~
This work is currently under review
~~~
The full paper describing the overall approach WILL BE available here [PDF](link)


## Credits
This algorithm has been developed by Vito Walter Anelli and Joseph Trotta while working at [SisInf Lab](http://sisinflab.poliba.it) under the supervision of Tommaso Di Noia.  

## Contacts

   Tommaso Di Noia, tommaso [dot] dinoia [at] poliba [dot] it  
   
   Vito Walter Anelli, vitowalter [dot] anelli [at] poliba [dot] it 
   
   Joseph Trotta, joseph [dot] trotta [at] poliba [dot] it 
