/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

public class InvertedIndex {

    private ArrayList<Document> listOfDocument = new ArrayList<Document>();
    private ArrayList<Term> dictionary = new ArrayList<Term>();

    public InvertedIndex() {
    }

    public void addNewDocument(Document document) {
        this.listOfDocument.add(document);
    }

    public ArrayList<Document> getListOfDocument() {
        return listOfDocument;
    }

    public void setListOfDocument(ArrayList<Document> listOfDocument) {
        this.listOfDocument = listOfDocument;
    }

    public ArrayList<Term> getDictionary() {
        return dictionary;
    }

    public void setDictionary(ArrayList<Term> dictionary) {
        this.dictionary = dictionary;
    }

    public ArrayList<Posting> search(String query) {
        String[] tempQuery = query.split(" ");
        ArrayList<Posting> tempPosting = new ArrayList<>();
        for (int i = 0; i < tempQuery.length; i++) {
            String string = tempQuery[i];
            if (i == 0) {
                tempPosting = searchOneWord(tempQuery[i]);
            } else {
                ArrayList<Posting> tempPosting1 = searchOneWord(tempQuery[i]);
                tempPosting = intersection(tempPosting, tempPosting1);
            }
        }
        return tempPosting;
    }

    public ArrayList<Posting> searchOneWord(String query) {
        Term tempTerm = new Term(query);
        if (getDictionary().isEmpty()) {
            return null;
        } else {
            int positionTerm = Collections.binarySearch(dictionary, tempTerm);
            if (positionTerm < 0) {
                return null;
            } else {
                return dictionary.get(positionTerm).getPostingList();
            }
        }
    }

    public ArrayList<Posting> intersection(ArrayList<Posting> p1,
            ArrayList<Posting> p2) {
        if (p1 == null || p2 == null) {
            return new ArrayList<>();
        }
        ArrayList<Posting> tempPostings = new ArrayList<>();
        int p1Index = 0;
        int p2Index = 0;

        Posting post1 = p1.get(p1Index);
        Posting post2 = p2.get(p2Index);

        while (true) {
            if (post1.getDocument().getId() == post2.getDocument().getId()) {
                try {
                    tempPostings.add(post1);
                    p1Index++;
                    p2Index++;

                    post1 = p1.get(p1Index);
                    post2 = p2.get(p2Index);
                } catch (Exception ex) {
                    break;
                }

            } 
            else if (post1.getDocument().getId() < post2.getDocument().getId()) {
                try {
                    p1Index++;
                    post1 = p1.get(p1Index);
                } catch (Exception ex) {
                    break;
                }

            } else {
                try {
                    p2Index++;
                    post2 = p2.get(p2Index);
                } catch (Exception ex) {
                    break;
                }
            }
        }
        return tempPostings;
    }

    public ArrayList<Posting> getUnsortedPostingList() {
        ArrayList<Posting> list = new ArrayList<Posting>();
        for (int i = 0; i < listOfDocument.size(); i++) {
            String[] termResult = listOfDocument.get(i).getListofTerm();
            for (int j = 0; j < termResult.length; j++) {
                Posting tempPosting = new Posting(termResult[j], listOfDocument.get(i));
                list.add(tempPosting);
            }
        }
        return list;
    }

    public ArrayList<Posting> getUnsortedPostingListWithTermNumber() {
        ArrayList<Posting> list = new ArrayList<Posting>();
        for (int i = 0; i < getListOfDocument().size(); i++) {
            ArrayList<Posting> postingDocument = getListOfDocument().get(i).getListofPosting();
            for (int j = 0; j < postingDocument.size(); j++) {
                Posting tempPosting = postingDocument.get(j);
                list.add(tempPosting);
            }
        }
        return list;
    }

    public ArrayList<Posting> getSortedPostingList() {
        ArrayList<Posting> list = new ArrayList<Posting>();
        list = this.getUnsortedPostingList();
        Collections.sort(list);
        return list;
    }

    public ArrayList<Posting> getSortedPostingListWithTermNumber() {
        ArrayList<Posting> list = new ArrayList<Posting>();
        list = this.getUnsortedPostingListWithTermNumber();
        Collections.sort(list);
        return list;
    }

    public void makeDictionary() {
        ArrayList<Posting> list = getSortedPostingList();
        for (int i = 0; i < list.size(); i++) {
            if (dictionary.isEmpty()) {
                Term term = new Term(list.get(i).getTerm());
                term.getPostingList().add(list.get(i));
                getDictionary().add(term);
            } else {
                Term tempTerm = new Term(list.get(i).getTerm());
                int position = Collections.binarySearch(dictionary, tempTerm);
                if (position < 0) {
                    tempTerm.getPostingList().add(list.get(i));
                    dictionary.add(tempTerm);
                } else {
                    dictionary.get(position).getPostingList().add(list.get(i));
                    Collections.sort(dictionary.get(position).getPostingList());
                }
                Collections.sort(dictionary);
            }
        }
    }

    public void makeDictionaryWithTermNumber() {
        ArrayList<Posting> list = getSortedPostingListWithTermNumber();
        for (int i = 0; i < list.size(); i++) {
            if (getDictionary().isEmpty()) {
                Term term = new Term(list.get(i).getTerm());
                term.getPostingList().add(list.get(i));
                getDictionary().add(term);
            } else {
                Term tempTerm = new Term(list.get(i).getTerm());
                int position = Collections.binarySearch(getDictionary(), tempTerm);
                if (position < 0) {
                    tempTerm.getPostingList().add(list.get(i));
                    getDictionary().add(tempTerm);
                } else {
                    getDictionary().get(position).
                            getPostingList().add(list.get(i));
                    Collections.sort(getDictionary().get(position)
                            .getPostingList());
                }
                Collections.sort(getDictionary());
            }
        }
    }

    public int getDocumentFrequency(String term) {
        Term tempTerm = new Term(term);
        int index = Collections.binarySearch(dictionary, tempTerm);
        if (index > 0) {
            ArrayList<Posting> tempPosting = dictionary.get(index)
                    .getPostingList();
            return tempPosting.size();
        } else {
            return -1;
        }
    }

    public double getInverseDocumentFrequency(String term) {
        Term tempTerm = new Term(term);
        int index = Collections.binarySearch(dictionary, tempTerm);
        if (index > 0) {
            int N = listOfDocument.size();
            int ni = getDocumentFrequency(term);
            double Nni = (double) N / ni;
            return Math.log10(Nni);
        } else {
            return 0.0;
        }
    }

    public int getTermFrequency(String term, int idDocument) {
        Document document = new Document();
        document.setId(idDocument);
        int pos = Collections.binarySearch(listOfDocument, document);
        if (pos >= 0) {
            ArrayList<Posting> tempPosting = listOfDocument.get(pos).getListofPosting();
            Posting posting = new Posting();
            posting.setTerm(term);
            int postingIndex = Collections.binarySearch(tempPosting, posting);
            if (postingIndex >= 0) {
                return tempPosting.get(postingIndex).getNumberOfTerm();
            }
            return 0;
        }

        return 0;
    }

    public ArrayList<Posting> makeTFIDF(int idDocument) {
        ArrayList<Posting> result = new ArrayList<Posting>();
        Document temp = new Document(idDocument);
        int cari = Collections.binarySearch(listOfDocument, temp);
        if (cari >= 0) {
            temp = listOfDocument.get(cari);
            result = temp.getListofPosting();
            for (int i = 0; i < result.size(); i++) {
                String tempTerm = result.get(i).getTerm();
                double idf = getInverseDocumentFrequency(tempTerm);
                int tf = result.get(i).getNumberOfTerm();
                double bobot = tf * idf;
                result.get(i).setWeight(bobot);
            }
            Collections.sort(result);
        } else {
        }
        return result;
    }

    public double getInnerProduct(ArrayList<Posting> p1, ArrayList<Posting> p2) {
        Collections.sort(p2);
        Collections.sort(p1);
        double result = 0.0;
        for (int i = 0; i < p1.size(); i++) {
            Posting temp = p1.get(i);
            boolean found = false;
            for (int j = 0; j < p2.size() && found == false; j++) {
                Posting temp1 = p2.get(j);
                if (temp1.getTerm().equalsIgnoreCase(temp.getTerm())) {
                    found = true;     
                    result = result + temp1.getWeight() * temp.getWeight();
                }
            }
        }
        return result;
    }

    public ArrayList<Posting> getQueryPosting(String query) {
        Document temp = new Document(-1, query);
        ArrayList<Posting> result = temp.getListofPosting();
        for (int i = 0; i < result.size(); i++) {
            String tempTerm = result.get(i).getTerm();
            double idf = getInverseDocumentFrequency(tempTerm);
            int tf = result.get(i).getNumberOfTerm();
            double bobot = tf * idf;
            result.get(i).setWeight(bobot);
        }
        Collections.sort(result);
        return result;
    }
    public double getLengthOfPosting(ArrayList<Posting> posting) {
        double result = 0.0;
        for (int i = 0; i < posting.size(); i++) {
            result = result + Math.pow(posting.get(i).getWeight(), 2);
        }
        double hasil = Math.sqrt(result);
        return hasil;
    }
    public double getCosineSimilarity(ArrayList<Posting> posting, ArrayList<Posting> posting1) {
        double atas = getInnerProduct(posting, posting1);
        double panjangPosting = getLengthOfPosting(posting);
        double panjangPosting1 = getLengthOfPosting(posting1);
        double hasil = atas / (Math.sqrt(panjangPosting * panjangPosting1));
        return hasil;
    }
    public ArrayList<SearchingResult> searchTFIDF(String query) {
        ArrayList<SearchingResult> hasil = new ArrayList<>();
        ArrayList<Posting> pQuery = getQueryPosting(query);
        for (int i = 0; i < listOfDocument.size(); i++) {
            ArrayList<Posting> tempDocWeight = makeTFIDF(listOfDocument.get(i).getId());
            double hasilDotProduct = getInnerProduct(tempDocWeight, pQuery);
            if (hasilDotProduct > 0) {
                SearchingResult hasilCari = new SearchingResult(hasilDotProduct, listOfDocument.get(i));
                hasil.add(hasilCari);
            }
        }
        Collections.sort(hasil);
        return hasil;
    }
    public ArrayList<SearchingResult> searchCosineSimilarity(String query) {
        ArrayList<SearchingResult> hasil = new ArrayList<>();
        ArrayList<Posting> pQuery = getQueryPosting(query);
        for (int i = 0; i < listOfDocument.size(); i++) {
            ArrayList<Posting> tempDocWeight = makeTFIDF(listOfDocument.get(i).getId());
            double Cosine = getCosineSimilarity(tempDocWeight, pQuery);
            if (Cosine > 0) {
                SearchingResult hasilCari = new SearchingResult(Cosine, listOfDocument.get(i));
                hasil.add(hasilCari);
            }
        }
        Collections.sort(hasil);
        return hasil;
    }

    public void readDirectory(File directory) {
        File files[] = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            Document doc = new Document();
            File file = files[i];
            doc.readFile(i + 1, file);
            doc.Stemming();
            doc.setNamaDokumen(file.getName());
            this.addNewDocument(doc);
        }
        this.makeDictionaryWithTermNumber();
    }
}
