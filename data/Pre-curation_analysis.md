# Data analysis

- Annotation process takes 1240 minutes (20.6 hours on crunch) over ChEMBL 17
- Commands are for MySQL

## Actionable metrics

- *Curation completion*: 100 x (#annotatedAssay.needReview = true) / (#annotatedAssay)
- *Curation quality*: 100 - ( 100 x (#Annotation.isFake = true) / (#InitialFakeAnnotations) )

## Beta test questions

- Random assays or keep order?
- Can we improve the rules?
- What information difference should it be between players and non players?

## Workflow over electronically annotated assays

Steps of the workflow after electronic annotations.

### Export electronically annotated database and save it somewhere.

db1 = source and db2 is target. db2 needs to be created before loading.

```
mysqldump -h [server] -u [user] -p[password] db1 | mysql -h [server] -u [user] -p[password] db2
```

### Mark as curated assays with 1 annotation and a confidence score greater or equal to 5 (curated by robot user).
```
UPDATE AnnotatedAssay
SET needReview = false, reviewer_id = 1
WHERE chemblId IN (
	SELECT chemblId
	FROM (
		SELECT AnnotatedAssay.chemblId, Annotation.confidence as confidence, COUNT(Annotation.assay_id) AS numberOfTerms
		FROM AnnotatedAssay
		LEFT JOIN Annotation ON AnnotatedAssay.id = Annotation.assay_id
		GROUP BY Annotation.assay_id
	) as x
	WHERE numberOfTerms = 1 AND confidence >= 5
)
```

### Flag for review assays with 1 annotation and a confidence score lower than 5 and assays with more than 1 annotation.
```
UPDATE AnnotatedAssay
SET needReview = true
WHERE chemblId IN (
	SELECT chemblId
	FROM (
		SELECT AnnotatedAssay.chemblId, Annotation.confidence as confidence, COUNT(Annotation.assay_id) AS numberOfTerms
		FROM AnnotatedAssay
		LEFT JOIN Annotation ON AnnotatedAssay.id = Annotation.assay_id
		GROUP BY Annotation.assay_id
	) as x
	WHERE numberOfTerms > 1
)
```

### Flag assays with 1 annotation but with a confidence inferior to 5
```
UPDATE AnnotatedAssay
SET needReview = true
WHERE chemblId IN (
	SELECT chemblId
	FROM (
		SELECT AnnotatedAssay.chemblId, Annotation.confidence as confidence, COUNT(Annotation.assay_id) AS numberOfTerms
		FROM AnnotatedAssay
		LEFT JOIN Annotation ON AnnotatedAssay.id = Annotation.assay_id
		GROUP BY Annotation.assay_id
	) as x
	WHERE numberOfTerms = 1 AND confidence < 5
)
```
### Generate around 5% of fake annotations (in `application.conf`), added to the assays to be curated only.

Using admin tools.

### Do the manual curation process.

Wait for curators to finish the job.

### Generate full curation report (actionable metrics).

Using admin tools.

### Remove the remaining fake annotations.

Using admin tools.

### Database is in curated state, ready to be incorporated to ChEMBL.

Using admin tools. To be implemented:
- Apply the last automatic annotation step: assays not annotated are mapped based on their current ChEMBL types.
- E.g. All binding assays (type = `B`) without annotations are asserted as `bao:BAO_0002989` (`binding assay`).
- Create two types of rules: the first round ones, to be curated, and the ones to fill the voids.

## Questions of interest

- How many assays have been validated by each curator (stats page)?
- What are the BAO terms distribution for the assays that will be manually curated (needReview = true)?
- What is the score of each curator?
- What is distribution of number of terms per assay?
- What is the percentage of ChEMBL assays annotated via the rules?
- How fast is the annotation process?

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
1 | 177,509
2 | 58,487
3 | 61,823
4 | 64,380
5 | 2,605
6 | 52
7 | 1

- Annotated assays and the number of annotations they have:
```
SELECT AnnotatedAssay.chemblId as chemblid, COUNT(Annotation.assay_id) AS numberOfTerms
FROM AnnotatedAssay, Annotation
WHERE AnnotatedAssay.id = Annotation.assay_id
GROUP BY Annotation.assay_id
```

- Retrieve all assays with one annotation and a high confidence score (assays to be automatically validated):
```
SELECT COUNT(chemblid) FROM ( 

SELECT AnnotatedAssay.chemblId as chemblid, Annotation.confidence as confidence, COUNT(Annotation.assay_id) AS numberOfTerms
FROM AnnotatedAssay
LEFT JOIN Annotation ON AnnotatedAssay.id = Annotation.assay_id 
GROUP BY Annotation.assay_id

) as x
WHERE numberOfTerms = 1
AND confidence >= 5
```
> 101,353

- Same query with update:
```
UPDATE AnnotatedAssay 
SET needReview = false
WHERE chemblId IN (
	SELECT chemblId
	FROM (
		SELECT AnnotatedAssay.chemblId, Annotation.confidence as confidence, COUNT(Annotation.assay_id) AS numberOfTerms
		FROM AnnotatedAssay
		LEFT JOIN Annotation ON AnnotatedAssay.id = Annotation.assay_id
		GROUP BY Annotation.assay_id
	) as x
	WHERE numberOfTerms = 1 AND confidence >= 5)
```

- Retrieve all assays with one annotation and a low confidence score (assays to be curated):
```
SELECT COUNT(chemblid) FROM ( 

SELECT AnnotatedAssay.chemblId as chemblid, Annotation.confidence as confidence, COUNT(Annotation.assay_id) AS numberOfTerms
FROM AnnotatedAssay
LEFT JOIN Annotation ON AnnotatedAssay.id = Annotation.assay_id 
GROUP BY Annotation.assay_id

) as x
WHERE numberOfTerms = 1
AND confidence < 5
```
> 76,156

- Retrieve all assays annotated with more than one term (to be curated):
```
SELECT COUNT(chemblid) FROM ( 

SELECT AnnotatedAssay.chemblId as chemblid, Annotation.confidence as confidence, COUNT(Annotation.assay_id) AS numberOfTerms
FROM AnnotatedAssay
LEFT JOIN Annotation ON AnnotatedAssay.id = Annotation.assay_id 
GROUP BY Annotation.assay_id

) as x
WHERE numberOfTerms > 1
```
> 187,348

- For all assays with one annotation, with low confidence (<5), assert whether the prediction is right or wrong
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

## Later analysis with zooma

- Keep a copy of the automatically annotated database
- Convert the manually curated database to zooma format
- Give numbers about time/speed
- Convert in zooma format (cf email Dani)
- Load the annotations inside zooma
- Annotate chembl database using zooma
- compare zooma outcome versus chembl outcome (automatic and electronic)
