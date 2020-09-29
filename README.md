# HybridFactorizationMachines
## Semantically Interpretable Factorization Machines

In the last decade, model-based recommender systems have shown their highly competitive performance in different domains and settings. In fact, they usually rely on the computation of latent factors to recommend items with a very high level of accuracy. Unfortunately, when moving to a latent space it is hard to keep references to the actual semantics of the recommended item, thus making the predictive model a black-box oracle.
In this work, we show how to exploit semantic features coming from knowledge graphs to properly initialize latent factors in Factorization Machines, thus training an interpretable model. In the presented approach, semantic features are injected into the learning process to retain the original informativeness of the items available in the catalog. An experimental evaluation on three different datasets shows the effectiveness of the obtained interpretable model in terms both of accuracy and diversity for recommendation results.

In this work, we propose a _knwowledge-aware Hybrid Factorization Machine_ (_kaHFM_) to train interpretable models in recommendation scenarios. _kaHFM_ relies on Factorization Machines and it extends them in different key aspects making use of the semantic information encoded in a knowledge graph.

With _kaHFM_ we build a model in which the meaning of each latent factor is bound to an explicit content-based feature extracted from a knowledge graph. Doing this, after the model has been trained, we still have an explicit reference to the original semantics of the features describing the items, thus making possible the interpretation of the final results. 

## Reference
If you publish research that uses _Hybrid Factorization Machines_ please use:

our recent article published in _IEEE Transactions on Knowledge and Data Engineering (TKDE)_. [The paper is available here](https://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=9143460)
~~~
@ARTICLE{9143460,
  author={V. W. {Anelli} and T. {Di Noia} and E. {Di Sciascio} and A. {Ragone} and J. {Trotta}},
  journal={IEEE Transactions on Knowledge and Data Engineering}, 
  title={Semantic Interpretation of Top-N Recommendations}, 
  year={2020},
  volume={},
  number={},
  pages={1-1},}
~~~
or our article published in the proceedings of _18th International Semantic Web Conference (ISWC2019) - Best Student Research Paper_ [The paper is available here](https://link.springer.com/content/pdf/10.1007%2F978-3-030-30793-6_3.pdf)
~~~
@inproceedings{DBLP:conf/semweb/AnelliNSRT19,
  author    = {Vito Walter Anelli and
               Tommaso Di Noia and
               Eugenio Di Sciascio and
               Azzurra Ragone and
               Joseph Trotta},
  editor    = {Chiara Ghidini and
               Olaf Hartig and
               Maria Maleshkova and
               Vojtech Sv{\'{a}}tek and
               Isabel F. Cruz and
               Aidan Hogan and
               Jie Song and
               Maxime Lefran{\c{c}}ois and
               Fabien Gandon},
  title     = {How to Make Latent Factors Interpretable by Feeding Factorization
               Machines with Knowledge Graphs},
  booktitle = {The Semantic Web - {ISWC} 2019 - 18th International Semantic Web Conference,
               Auckland, New Zealand, October 26-30, 2019, Proceedings, Part {I}},
  series    = {Lecture Notes in Computer Science},
  volume    = {11778},
  pages     = {38--56},
  publisher = {Springer},
  year      = {2019},
  url       = {https://doi.org/10.1007/978-3-030-30793-6\_3},
  doi       = {10.1007/978-3-030-30793-6\_3},
  timestamp = {Sat, 05 Sep 2020 18:02:34 +0200},
  biburl    = {https://dblp.org/rec/conf/semweb/AnelliNSRT19.bib},
  bibsource = {dblp computer science bibliography, https://dblp.org}
}
~~~


## Credits
This algorithm has been developed by Vito Walter Anelli and Joseph Trotta while working at [SisInf Lab](http://sisinflab.poliba.it) under the supervision of Tommaso Di Noia.  

## Contacts

   Tommaso Di Noia, tommaso [dot] dinoia [at] poliba [dot] it  
   
   Vito Walter Anelli, vitowalter [dot] anelli [at] poliba [dot] it 
   
   Joseph Trotta, joseph [dot] trotta [at] poliba [dot] it 
