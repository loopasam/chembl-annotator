# Charaterization of automated annotations

- Annotation process takes 1240 minutes (20.6 hours on crunch) over ChEMBL 17
- Commands are for MySQL

## Coverage of ChEMBL assays

- Number of annotated assays:
`SELECT COUNT(DISTINCT id) FROM AnnotatedAssay`
> 364,857

- Number of assays:
`SELECT COUNT(DISTINCT assay_id) FROM assays`
> 734,201

- Percent of assays automatically annotated:
> 49.7%

## Annotations' distributions

- BAO annotated terms' distribution:
```
SELECT BaoTerm.label, count(AnnotatedAssay.id) as pound
FROM AnnotatedAssay
INNER JOIN Annotation ON AnnotatedAssay.id = Annotation.assay_id
INNER JOIN BaoTerm ON Annotation.term_id = BaoTerm.id
GROUP BY BaoTerm.label
ORDER BY pound DESC
```

term | # of assays annotated
--- | ---
binding assay | 199621
protein-small molecule interaction assay | 166484
enzyme activity assay | 98188
cytotoxicity assay | 41495
radioligand binding assay | 39604
kinase activity assay | 37467
cell growth assay | 25587
pharmacokinetic assay | 22413
protease activity assay | 15465
tissue distribution assay | 11934
ion channel assay | 9530
transporter assay | 8427
cell cycle assay | 8006
apoptosis assay | 7665
bioavailability assay | 6929
cytochrome P450 enzyme activity assay | 6595
phosphorylation assay | 5295
cell viability assay | 4482
oxidoreductase activity assay | 3728
hydrolase activity assay | 3674
reporter gene assay | 3477
gene expression assay | 3018
cell permeability assay | 2855
bioassay | 2566
luciferase reporter gene assay | 2450
cell proliferation assay | 1991
lyase activity assay | 1757
membrane potential assay | 1726
phosphatase activity assay | 1580
isomerase activity assay | 1246
second messenger assay | 1180
transferase activity assay | 861
pharmacodynamic assay | 834
cell motility assay | 683
cell morphology assay | 377
genotoxicity assay | 316
localization assay | 229
neurite outgrowth assay | 155
mitochondrial membrane potential assay | 152
oxidative stress assay | 145
metastasis assay | 119
protein-protein interaction assay | 115
sensitizer assay | 111
QT interval assay | 78
redistribution assay | 54
protein stability assay | 31
signal transduction assay | 28
beta galactosidase reporter gene assay | 19
platelet activation assay | 14
rna splicing assay | 11
beta lactamase reporter gene assay | 11
KINOMEScan assay | 10
drug interaction assay | 7
protein unfolding assay | 6
chaperone activity assay | 5
oxidative phosphorylation assay | 4
cytokine secretion assay | 3
fluorescent protein reporter gene assay | 1
protein-RNA interaction assay | 1
protein-DNA interaction assay | 1

- Confidence level of annotations:
`SELECT COUNT(DISTINCT id) FROM Annotation WHERE confidence = $`

Confidence score | Number of annotations
--- | ---
inf. 1 | 0
1 | 132,304
2 | 12,556
3 | 299
4 | 6738
5 | 538,172
sup. 5 | 60,747

## Annotation per assay

- Number of annotations:
`SELECT COUNT(DISTINCT Annotation.id) FROM Annotation`
> 750,816

- Number of annotation per annotated assay:
> 2.06

- Distribution of number of annotation per assay:
```
SELECT numberOfTerms, COUNT(chemblid) as numberOfAssay FROM (

SELECT AnnotatedAssay.chemblId as chemblid, COUNT(Annotation.assay_id) AS numberOfTerms
FROM AnnotatedAssay, Annotation
WHERE AnnotatedAssay.id = Annotation.assay_id
GROUP BY Annotation.assay_id
ORDER BY numberOfTerms DESC

) as x
GROUP BY numberOfTerms
```
Number of annotated terms | number of annotated assays
--- | ---
1 | 177509
2 | 58487
3 | 61823
4 | 64380
5 | 2605
6 | 52
7 | 1

- Curation task: for all assays with one annotation, with low confidence (<5), assert whether the prediction is right or wrong
```
SELECT numberOfTerms, COUNT(chemblid) as numberOfAssay FROM (

SELECT AnnotatedAssay.chemblId as chemblid, COUNT(Annotation.assay_id) AS numberOfTerms
FROM AnnotatedAssay, Annotation
WHERE AnnotatedAssay.id = Annotation.assay_id
AND Annotation.confidence < 5
GROUP BY Annotation.assay_id

) as x
WHERE numberOfTerms = 1
GROUP BY numberOfTerms
```
