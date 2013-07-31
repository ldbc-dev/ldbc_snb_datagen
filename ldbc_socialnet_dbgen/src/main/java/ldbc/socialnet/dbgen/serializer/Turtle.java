/*
 * Copyright (c) 2013 LDBC
 * Linked Data Benchmark Council (http://ldbc.eu)
 *
 * This file is part of ldbc_socialnet_dbgen.
 *
 * ldbc_socialnet_dbgen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ldbc_socialnet_dbgen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ldbc_socialnet_dbgen.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2011 OpenLink Software <bdsmt@openlinksw.com>
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation;  only Version 2 of the License dated
 * June 1991.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ldbc.socialnet.dbgen.serializer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import ldbc.socialnet.dbgen.dictionary.IPAddressDictionary;
import ldbc.socialnet.dbgen.dictionary.LanguageDictionary;
import ldbc.socialnet.dbgen.dictionary.LocationDictionary;
import ldbc.socialnet.dbgen.dictionary.TagDictionary;
import ldbc.socialnet.dbgen.generator.DateGenerator;
import ldbc.socialnet.dbgen.objects.Comment;
import ldbc.socialnet.dbgen.objects.Friend;
import ldbc.socialnet.dbgen.objects.Group;
import ldbc.socialnet.dbgen.objects.GroupMemberShip;
import ldbc.socialnet.dbgen.objects.Location;
import ldbc.socialnet.dbgen.objects.Photo;
import ldbc.socialnet.dbgen.objects.Post;
import ldbc.socialnet.dbgen.objects.ReducedUserProfile;
import ldbc.socialnet.dbgen.objects.UserExtraInfo;
import ldbc.socialnet.dbgen.vocabulary.DBP;
import ldbc.socialnet.dbgen.vocabulary.DBPOWL;
import ldbc.socialnet.dbgen.vocabulary.DBPPROP;
import ldbc.socialnet.dbgen.vocabulary.FOAF;
import ldbc.socialnet.dbgen.vocabulary.RDF;
import ldbc.socialnet.dbgen.vocabulary.RDFS;
import ldbc.socialnet.dbgen.vocabulary.SN;
import ldbc.socialnet.dbgen.vocabulary.SNVOC;
import ldbc.socialnet.dbgen.vocabulary.XSD;


public class Turtle implements Serializer {
	
    private static final String STATIC_DBP_DATA_FILE = "static_dbp";
    
    private boolean isTurtle;
	private long nrTriples;
	private FileWriter[] dataFileWriter;
	private FileWriter[] staticdbpFileWriter;
	int currentWriter = 0;
	static long membershipId = 0;
	static long friendshipId = 0; 
	static long gpsId = 0; 
	static long likeId = 0;
	static long workatId = 0;
	static long studyAt = 0;
	static long locationPartOfId = 0;
	static long speakId = 0;
	
	HashMap<String, Integer> companyToCountry;
	HashMap<String, Integer> universityToCountry;
	HashMap<String, Integer> printedIpaddresses;
	HashMap<String, Integer> printedOrganizations;
	HashMap<Integer, Integer> printedLanguages;
	HashMap<Integer, Integer> printedTags;
	HashMap<Integer, Integer> printedTagClasses;
	HashMap<Integer, Integer> printedLocations;
	Vector<String>	vBrowserNames;
	
	GregorianCalendar date;
	LocationDictionary locationDic;
	LanguageDictionary languageDic;
	TagDictionary tagDic;
	IPAddressDictionary ipDic;
	
	public Turtle(String file, boolean forwardChaining, int nrOfOutputFiles, boolean isTurtle) {
	    this.isTurtle = isTurtle;
		date = new GregorianCalendar();
		printedIpaddresses = new HashMap<String, Integer>();
		printedOrganizations = new HashMap<String, Integer>();
		printedLanguages = new HashMap<Integer, Integer>();
		printedTags = new HashMap<Integer, Integer>();
		printedTagClasses = new HashMap<Integer, Integer>();
		printedLocations = new HashMap<Integer, Integer>();
		int nrOfDigits = ((int)Math.log10(nrOfOutputFiles)) + 1;
		String formatString = "%0" + nrOfDigits + "d";
		try{
		    String extension = (isTurtle) ? ".ttl": ".n3";
			dataFileWriter = new FileWriter[nrOfOutputFiles];
			staticdbpFileWriter = new FileWriter[nrOfOutputFiles];
			if(nrOfOutputFiles==1) {
				this.dataFileWriter[0] = new FileWriter(file + extension);
				this.staticdbpFileWriter[0] = new FileWriter(file+STATIC_DBP_DATA_FILE + extension);
			} else {
				for(int i=1;i<=nrOfOutputFiles;i++) {
					dataFileWriter[i-1] = new FileWriter(file + String.format(formatString, i) + extension);
					this.staticdbpFileWriter[i-1] = new FileWriter(file+STATIC_DBP_DATA_FILE + String.format(formatString, i) + extension);
				}
			}
				
		} catch(IOException e) {
			System.err.println("Could not open File for writing.");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		try {
			for(int i=0;i<nrOfOutputFiles;i++) {
				dataFileWriter[i].append(getNamespaces());
			}
			for(int i=0;i<nrOfOutputFiles;i++) {
                staticdbpFileWriter[i].append(getStaticNamespaces());
            }
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
		
		nrTriples = 0l;
	}

	public Turtle(String file, boolean forwardChaining, int nrOfOutputFiles, boolean isTurtle,
	        TagDictionary tagDic, Vector<String> _vBrowsers, 
	        HashMap<String, Integer> companyToCountry, HashMap<String, Integer> univesityToCountry,
	        IPAddressDictionary ipDic,  LocationDictionary locationDic, 
	        LanguageDictionary languageDic) {
	    this(file, forwardChaining, nrOfOutputFiles, isTurtle);
	    this.tagDic = tagDic;  
	    this.vBrowserNames = _vBrowsers;
	    this.locationDic = locationDic;
	    this.companyToCountry = companyToCountry;
	    this.universityToCountry = univesityToCountry;
	    this.ipDic = ipDic;
	    this.languageDic = languageDic;
	}

	public Long triplesGenerated() {
		return nrTriples;
	}

	public void toWriter(String data){
	    try {
	        dataFileWriter[currentWriter].append(data);
	        currentWriter = (currentWriter + 1) % dataFileWriter.length;
	    } catch(IOException e){
	        System.out.println("Cannot write to output file ");
	        e.printStackTrace();
	        System.exit(-1);
	    }
	}
	
	private String getNamespaces() {
	    StringBuffer result = new StringBuffer();
		createPrefixLine(result, RDF.PREFIX, RDF.NS);
		createPrefixLine(result, RDFS.PREFIX, RDFS.NS);
		createPrefixLine(result, XSD.PREFIX, XSD.NS);
		createPrefixLine(result, SNVOC.PREFIX, SNVOC.NS);
		createPrefixLine(result, SN.PREFIX, SN.NS);
		createPrefixLine(result, DBP.PREFIX, DBP.NS);
		return result.toString();
	}
	
	private String getStaticNamespaces() {
        StringBuffer result = new StringBuffer();
        createPrefixLine(result, RDF.PREFIX, RDF.NS);
        createPrefixLine(result, RDFS.PREFIX, RDFS.NS);
        createPrefixLine(result, FOAF.PREFIX, FOAF.NS);
        createPrefixLine(result, DBP.PREFIX, DBP.NS);
        createPrefixLine(result, DBPOWL.PREFIX, DBPOWL.NS);
        createPrefixLine(result, DBPPROP.PREFIX, DBPPROP.NS);
        createPrefixLine(result, SNVOC.PREFIX, SNVOC.NS);
        return result.toString();
    }
	
	private void createPrefixLine(StringBuffer result, String prefix, String namespace) {
		result.append("@prefix ");
		result.append(prefix);
		result.append(" ");
		result.append(createURIref(namespace));
		result.append(" .\n");
	}

	private String createURIref(String uri) {
		StringBuffer result = new StringBuffer();
		result.append("<");
		result.append(uri);
		result.append(">");
		return result.toString();
	}
	
	private void writeDBPData(String left, String middle, String right) {
	    try {
	        StringBuffer result = new StringBuffer();
	        createTripleSPO(result, left, middle, right);
	        staticdbpFileWriter[currentWriter].append(result);
	    } catch (IOException e) {
            System.out.println("Cannot write to output file ");
            e.printStackTrace();
        }
	}
	
	private void writeTagData(StringBuffer result, Integer tagId, String tag) {
	    String description = tagDic.getDescription(tagId);
	    writeDBPData(DBP.fullPrefixed(tag), FOAF.Name, createLiteral(description.replace("\"", "\\\"")));
	    Integer tagClass = tagDic.getTagClass(tagId);
	    writeDBPData(DBP.fullPrefixed(tag), RDF.type, DBPOWL.prefixed(tagDic.getClassName(tagClass)));
	    while (tagClass != -1 && !printedTagClasses.containsKey(tagClass)) {
	        printedTagClasses.put(tagClass, tagClass);
	        if (tagDic.getClassName(tagClass).equals("Thing")) {
	            writeDBPData("<http://www.w3.org/2002/07/owl#Thing>", RDFS.label, createLiteral(tagDic.getClassLabel(tagClass)));
	            createTripleSPO(result, "<http://www.w3.org/2002/07/owl#Thing>", RDF.type, SNVOC.TagClass);
	        } else {
	            writeDBPData(DBPOWL.prefixed(tagDic.getClassName(tagClass)), RDFS.label, createLiteral(tagDic.getClassLabel(tagClass)));
	            createTripleSPO(result, DBP.fullPrefixed(tagDic.getClassName(tagClass)), RDF.type, SNVOC.TagClass);
	        }
	        Integer parent = tagDic.getClassParent(tagClass);
	        if (parent != -1) {
	            String parentPrefix;
	            if (tagDic.getClassName(parent).equals("Thing")) {
	                parentPrefix = "<http://www.w3.org/2002/07/owl#Thing>";
	            } else {
	                parentPrefix = DBPOWL.prefixed(tagDic.getClassName(parent));
	            }
	            writeDBPData(DBPOWL.prefixed(tagDic.getClassName(tagClass)), RDFS.subClassOf, parentPrefix);
	        }
	        tagClass = parent;
	    }
	}
	
	private void AddTriple(StringBuffer result, boolean beginning, 
            boolean end, String left, String middle, String right, String extra, boolean ignoreExtra) {
	    if (isTurtle && beginning) {
            result.append(left + "\n");
	    }
        if (isTurtle && end){
            if (extra.isEmpty()) {
                result.append(createTriplePOEnd(middle, right));
            } else {
                result.append(createTriplePOOEnd(middle, right, extra));
            }
        } else if (isTurtle) {
            if (extra.isEmpty()) {
                result.append(createTriplePO(middle, right));
            } else {
                result.append(createTriplePOO(middle, right, extra));
            }
        } else {
            createTripleSPO(result, left, middle, right);
            if (!extra.isEmpty() && !ignoreExtra) {
                createTripleSPO(result, left, middle, extra);
            }
        }
	}
	
	private void AddTriple(StringBuffer result, boolean beginning, 
	        boolean end, String left, String middle, String right) {
	    AddTriple(result, beginning, end, left, middle, right, "", false);
	}
	
	public void printLocationHierarchy(StringBuffer result, int baseId) {
	    ArrayList<Integer> areas = new ArrayList<Integer>();
        do {
            areas.add(baseId);
            baseId = locationDic.belongsTo(baseId);
        } while (baseId != -1);
        
        for (int i = areas.size() - 1; i >= 0; i--) {
            if (!printedLocations.containsKey(areas.get(i))) {
                String name = locationDic.getLocationName(areas.get(i));
                printedLocations.put(areas.get(i), areas.get(i));
                String type = DBPOWL.City;
                if (locationDic.getType(areas.get(i)) == Location.COUNTRY) {
                    type = DBPOWL.Country;
                } else if (locationDic.getType(areas.get(i)) == Location.CONTINENT) {
                    type = DBPOWL.Continent;
                }
                
                writeDBPData(DBP.fullPrefixed(name), RDF.type, DBPOWL.Place);
                writeDBPData(DBP.fullPrefixed(name), RDF.type, type);
                writeDBPData(DBP.fullPrefixed(name), FOAF.Name, createLiteral(name));
                if (locationDic.getType(areas.get(i)) != Location.CONTINENT) {
                    String countryName = locationDic.getLocationName(areas.get(i+1));
                    createTripleSPO(result, DBP.fullPrefixed(name), SNVOC.isPartOf, DBP.fullPrefixed(countryName));
                }
            }
        }
	}
	
	public void gatherData(ReducedUserProfile profile, UserExtraInfo extraInfo){
		StringBuffer result = new StringBuffer();
		
		if (extraInfo == null) {
		    System.err.println("LDBC socialnet must serialize the extraInfo");
		    System.exit(-1);
		}

		printLocationHierarchy(result, extraInfo.getLocationId());
		Iterator<String> itString = extraInfo.getCompanies().iterator();
		while (itString.hasNext()) {
		    String company = itString.next();
		    int parentId = companyToCountry.get(company);
		    printLocationHierarchy(result, parentId);
		}
		printLocationHierarchy(result, universityToCountry.get(extraInfo.getOrganization()));
		printLocationHierarchy(result, ipDic.getLocation(profile.getIpAddress()));
		
		
		String prefix = SN.getPersonURI(profile.getAccountId());
		AddTriple(result, true, false, prefix, RDF.type, SNVOC.Person);

		AddTriple(result, false, false, prefix, SNVOC.firstName, 
		        createLiteral(extraInfo.getFirstName()));

		AddTriple(result, false, false, prefix, SNVOC.lastName, 
		        createLiteral(extraInfo.getLastName()));

		if (!extraInfo.getGender().equals("")) {
		    AddTriple(result, false, false, prefix, SNVOC.gender,
		            createLiteral(extraInfo.getGender()));
		}

		if (profile.getBirthDay() != -1 ){
		    date.setTimeInMillis(profile.getBirthDay());
		    String dateString = DateGenerator.formatDate(date);
		    AddTriple(result, false, false, prefix, SNVOC.birthday,
		            createDataTypeLiteral(dateString, XSD.Date));
		}
		
		if (profile.getIpAddress() != null) {
            AddTriple(result, false, false, prefix, SNVOC.ipaddress, 
                    createLiteral(profile.getIpAddress().toString()));
            if (profile.getBrowserIdx() >= 0) {
                AddTriple(result, false, false, prefix, SNVOC.browser,
                        createLiteral(vBrowserNames.get(profile.getBrowserIdx())));
            }
        }

        date.setTimeInMillis(profile.getCreatedDate());
        String dateString = DateGenerator.formatDateDetail(date);
        AddTriple(result, false, true, prefix, SNVOC.creationDate,
                createDataTypeLiteral(dateString, XSD.DateTime));

        createTripleSPO(result, prefix, SNVOC.locatedIn, DBP.fullPrefixed(locationDic.getLocationName(extraInfo.getLocationId())));

        if (!extraInfo.getOrganization().equals("")) {
            if (!printedOrganizations.containsKey(extraInfo.getOrganization())) {
                int organizationId = printedOrganizations.size();
                printedOrganizations.put(extraInfo.getOrganization(), organizationId);
                
                writeDBPData(DBP.fullPrefixed(extraInfo.getOrganization()), RDF.type, DBPOWL.Organisation);
                writeDBPData(DBP.fullPrefixed(extraInfo.getOrganization()), FOAF.Name, createLiteral(extraInfo.getOrganization()));
                int locationId = universityToCountry.get(extraInfo.getOrganization());
                createTripleSPO(result, DBP.fullPrefixed(extraInfo.getOrganization()), 
                        SNVOC.locatedIn, DBP.fullPrefixed(locationDic.getLocationName(locationId)));
            }
            createTripleSPO(result, prefix, SNVOC.studyAt, SN.getStudyAtURI(studyAt));
            createTripleSPO(result, SN.getStudyAtURI(studyAt), SNVOC.hasOrganisation, 
                    DBP.fullPrefixed(extraInfo.getOrganization()));
            if (extraInfo.getClassYear() != -1 ){
                date.setTimeInMillis(extraInfo.getClassYear());
                String yearString = DateGenerator.formatYear(date);
                createTripleSPO(result, SN.getStudyAtURI(studyAt), SNVOC.classYear,
                        createLiteral(yearString));
            }

            studyAt++;
        }

        Vector<Integer> languages = extraInfo.getLanguages();
        for (int i = 0; i < languages.size(); i++) {
            createTripleSPO(result, prefix, SNVOC.speaks, 
                    createLiteral(languageDic.getLanguagesName(languages.get(i))));
            speakId++;
        }

        itString = extraInfo.getCompanies().iterator();
        while (itString.hasNext()) {
            String company = itString.next();
            if (!printedOrganizations.containsKey(company)) {
                int organizationId = printedOrganizations.size();
                printedOrganizations.put(company, organizationId);
                
                writeDBPData(DBP.fullPrefixed(company), RDF.type, DBPOWL.Organisation);
                writeDBPData(DBP.fullPrefixed(company), FOAF.Name, createLiteral(company));
                int locationId = companyToCountry.get(company);
                createTripleSPO(result, DBP.fullPrefixed(company), 
                        SNVOC.locatedIn, DBP.fullPrefixed(locationDic.getLocationName(locationId)));
            }

            createTripleSPO(result, prefix, SNVOC.workAt, SN.getWorkAtURI(workatId));
            createTripleSPO(result, SN.getWorkAtURI(workatId), SNVOC.hasOrganisation, 
                    DBP.fullPrefixed(company));
            date.setTimeInMillis(extraInfo.getWorkFrom(company));
            String yearString = DateGenerator.formatYear(date);
            createTripleSPO(result, SN.getWorkAtURI(workatId), SNVOC.workFrom,
                    createLiteral(yearString));

            workatId++;
        }

        itString = extraInfo.getEmail().iterator();
        while (itString.hasNext()){
            String email = itString.next();
            createTripleSPO(result, prefix, SNVOC.email, createLiteral(email));
        }
		
        Iterator<Integer> itInteger = profile.getSetOfTags().iterator();
		while (itInteger.hasNext()) {
			Integer interestIdx = itInteger.next();
			String interest = tagDic.getName(interestIdx);
			createTripleSPO(result, prefix, SNVOC.hasInterest, DBP.fullPrefixed(interest));  
			if (!printedTags.containsKey(interestIdx)) {
			    printedTags.put(interestIdx, interestIdx);
			    createTripleSPO(result, DBP.fullPrefixed(interest), RDF.type, SNVOC.Tag);
			    writeTagData(result, interestIdx, interest);
			}
		}
		
		Friend friends[] = profile.getFriendList();
		for (int i = 0; i < friends.length; i ++){
			if (friends[i] != null){
				if (friends[i].getCreatedTime() != -1){
				    createTripleSPO(result, prefix, SNVOC.knows,
				            SN.getPersonURI(friends[i].getFriendAcc()));
				}
			}
		}
		
		//User wall
        String title = "Wall of " + extraInfo.getFirstName() + " " + extraInfo.getLastName();
        String forumPrefix = SN.getForumURI(profile.getForumWallId());
        AddTriple(result, true, false, forumPrefix, RDF.type, SNVOC.Forum);
        AddTriple(result, false, false, forumPrefix, SNVOC.title, createLiteral(title));
        AddTriple(result, false, false, forumPrefix, SNVOC.creationDate, 
                createDataTypeLiteral(dateString, XSD.DateTime));
        AddTriple(result, false, true, forumPrefix, SNVOC.hasModerator, prefix);
        
        itInteger = profile.getSetOfTags().iterator();
        while (itInteger.hasNext()) {
            Integer interestIdx = itInteger.next();
            String interest = tagDic.getName(interestIdx);
            createTripleSPO(result, forumPrefix, SNVOC.hasTag, DBP.fullPrefixed(interest));
        }
        
        
        for (int i = 0; i < friends.length; i ++){
            if (friends[i] != null){
                if (friends[i].getCreatedTime() != -1){

                    String memberhipPrefix = SN.getMembershipURI(membershipId);
                    createTripleSPO(result, forumPrefix, SNVOC.hasMember, memberhipPrefix);

                    AddTriple(result, true, false, memberhipPrefix, SNVOC.hasPerson, SN.getPersonURI(friends[i].getFriendAcc()));
                    date.setTimeInMillis(friends[i].getCreatedTime());
                    dateString = DateGenerator.formatDateDetail(date);
                    AddTriple(result, false, true, memberhipPrefix, SNVOC.joinDate,
                            createDataTypeLiteral(dateString, XSD.DateTime));

                    membershipId++;
                }
            }
        }
		
		toWriter(result.toString());
	}

	public void gatherData(Post post){
	    StringBuffer result = new StringBuffer();

	    if (post.getIpAddress() != null) {
	        printLocationHierarchy(result, ipDic.getLocation(post.getIpAddress()));
	    }
	    date.setTimeInMillis(post.getCreatedDate());
	    String dateString = DateGenerator.formatDateDetail(date);
	    String prefix = SN.getPostURI(post.getPostId());

	    AddTriple(result, true, false, prefix, RDF.type, SNVOC.Post);
	    AddTriple(result, false, false, prefix, SNVOC.creationDate, 
	            createDataTypeLiteral(dateString, XSD.DateTime));
	    if (post.getIpAddress() != null) {
	        AddTriple(result, false, false, prefix, SNVOC.ipaddress, 
	                createLiteral(post.getIpAddress().toString()));
	        if (post.getBrowserIdx() >= 0) {
	            AddTriple(result, false, false, prefix, SNVOC.browser,
	                    createLiteral(vBrowserNames.get(post.getBrowserIdx())));
	        }
	    }
	    AddTriple(result, false, true, prefix, SNVOC.content,
	            createLiteral(post.getContent()));

	    if (post.getLanguage() != -1) {
	        createTripleSPO(result, prefix, SNVOC.language,
	                createLiteral(languageDic.getLanguagesName(post.getLanguage())));
	    }

	    if (post.getIpAddress() != null) {;
	    createTripleSPO(result, prefix, SNVOC.locatedIn,
	            DBP.fullPrefixed(locationDic.getLocationName((ipDic.getLocation(post.getIpAddress())))));
	    }

	    createTripleSPO(result, SN.getForumURI(post.getForumId()),SNVOC.containerOf, prefix);

	    createTripleSPO(result, prefix, SNVOC.hasCreator, SN.getPersonURI(post.getAuthorId()));

	    Iterator<Integer> it = post.getTags().iterator();
	    while (it.hasNext()) {
	        Integer tagId = it.next();
	        String tag = tagDic.getName(tagId);
	        createTripleSPO(result, prefix, SNVOC.hasTag, DBP.fullPrefixed(tag));
	        if (!printedTags.containsKey(tagId)) {
	            printedTags.put(tagId, tagId);
	            createTripleSPO(result, DBP.fullPrefixed(tag), RDF.type, SNVOC.Tag);
	            writeTagData(result, tagId, tag);
	        }
	    }

	    int userLikes[] = post.getInterestedUserAccs();
	    long likeTimestamps[] = post.getInterestedUserAccsTimestamp();

	    for (int i = 0; i < userLikes.length; i ++) {
	        String likePrefix = SN.getLikeURI(likeId);
	        createTripleSPO(result, SN.getPersonURI(userLikes[i]), 
	                SNVOC.like, likePrefix);

	        AddTriple(result, true, false, likePrefix, SNVOC.hasPost, prefix);
	        date.setTimeInMillis(likeTimestamps[i]);
	        dateString = DateGenerator.formatDateDetail(date);
	        AddTriple(result, false, true, likePrefix, SNVOC.creationDate,
	                createDataTypeLiteral(dateString, XSD.DateTime));
	        likeId++;
	    }

	    toWriter(result.toString());
	}

	public void gatherData(Comment comment){
		StringBuffer result = new StringBuffer();
		
		String prefix = SN.getCommentURI(comment.getCommentId());
		date.setTimeInMillis(comment.getCreateDate());
		String dateString = DateGenerator.formatDateDetail(date); 
		
		AddTriple(result, true, false, prefix, RDF.type, SNVOC.Comment);
		AddTriple(result, false, false, prefix, SNVOC.creationDate, createDataTypeLiteral(dateString, XSD.DateTime));
		if (comment.getIpAddress() != null) {
		    AddTriple(result, false, false, prefix, SNVOC.ipaddress, 
		            createLiteral(comment.getIpAddress().toString()));
		    if (comment.getBrowserIdx() >= 0) {
		        AddTriple(result, false, false, prefix, SNVOC.browser,
		                createLiteral(vBrowserNames.get(comment.getBrowserIdx())));
		    }
		}
		AddTriple(result, false, true, prefix, SNVOC.content, createLiteral(comment.getContent()));

		String replied = (comment.getReply_of() == -1) ? SN.getPostURI(comment.getPostId()) : 
		    SN.getCommentURI(comment.getReply_of());
		createTripleSPO(result, prefix, SNVOC.replyOf, replied);
		if (comment.getIpAddress() != null) {
		    createTripleSPO(result, prefix, SNVOC.locatedIn,
		            DBP.fullPrefixed(locationDic.getLocationName((ipDic.getLocation(comment.getIpAddress())))));
		}

		createTripleSPO(result, prefix, SNVOC.hasCreator,
		        SN.getPersonURI(comment.getAuthorId()));

		toWriter(result.toString());
	}
	
	public void gatherData(Photo photo){
	    StringBuffer result = new StringBuffer();

	    String prefix = SN.getPostURI(photo.getPhotoId());
	    AddTriple(result, true, false, prefix, RDF.type, SNVOC.Post);
	    AddTriple(result, false, false, prefix, SNVOC.hasImage, createLiteral(photo.getImage()));
	    date.setTimeInMillis(photo.getTakenTime());
	    String dateString = DateGenerator.formatDateDetail(date);
	    if (photo.getIpAddress() != null) {
	        AddTriple(result, false, false, prefix, SNVOC.ipaddress, 
	                createLiteral(photo.getIpAddress().toString()));
	        if (photo.getBrowserIdx() >= 0) {
	            AddTriple(result, false, false, prefix, SNVOC.browser,
	                    createLiteral(vBrowserNames.get(photo.getBrowserIdx())));
	        }
	    }
	    AddTriple(result, false, true, prefix, SNVOC.creationDate, 
	            createDataTypeLiteral(dateString, XSD.DateTime));

	    createTripleSPO(result, prefix, SNVOC.hasCreator, SN.getPersonURI(photo.getCreatorId()));
	    createTripleSPO(result, SN.getForumURI(photo.getAlbumId()), SNVOC.containerOf, prefix);
	    createTripleSPO(result, prefix, SNVOC.locatedIn,
	            DBP.fullPrefixed(locationDic.getLocationName((ipDic.getLocation(photo.getIpAddress())))));

	    Iterator<Integer> it = photo.getTags().iterator();
	    while (it.hasNext()) {
	        Integer tagId = it.next();
	        String tag = tagDic.getName(tagId);
	        createTripleSPO(result, prefix, SNVOC.hasTag, DBP.fullPrefixed(tag));
	        if (!printedTags.containsKey(tagId)) {
	            printedTags.put(tagId, tagId);
	            createTripleSPO(result, DBP.fullPrefixed(tag), RDF.type, SNVOC.Tag);
	            writeTagData(result, tagId, tag);
	        }
	    }

	    int userLikes[] = photo.getInterestedUserAccs();
	    long likeTimestamps[] = photo.getInterestedUserAccsTimestamp();
	    for (int i = 0; i < userLikes.length; i ++) {
	        String likePrefix = SN.getLikeURI(likeId);
	        createTripleSPO(result, SN.getPersonURI(userLikes[i]), 
	                SNVOC.like, likePrefix);

	        AddTriple(result, true, false, likePrefix, SNVOC.hasPost, prefix);
	        date.setTimeInMillis(likeTimestamps[i]);
	        dateString = DateGenerator.formatDateDetail(date);
	        AddTriple(result, false, true, likePrefix, SNVOC.creationDate,
	                createDataTypeLiteral(dateString, XSD.DateTime));
	        likeId++;
	    }

	    toWriter(result.toString());
	}

	public void gatherData(Group group){
	    StringBuffer result = new StringBuffer();
	    date.setTimeInMillis(group.getCreatedDate());
	    String dateString = DateGenerator.formatDateDetail(date);  

	    // Forums of the group
	    String forumPrefix = SN.getForumURI(group.getForumWallId());
	    AddTriple(result, true, false, forumPrefix, RDF.type, SNVOC.Forum);
	    AddTriple(result, false, false, forumPrefix, SNVOC.title, createLiteral(group.getGroupName()));
	    AddTriple(result, false, true, forumPrefix, SNVOC.creationDate, 
	            createDataTypeLiteral(dateString, XSD.DateTime));

	    createTripleSPO(result, forumPrefix,
	            SNVOC.hasModerator, SN.getPersonURI(group.getModeratorId()));

	    Integer groupTags[] = group.getTags();
	    for (int i = 0; i < groupTags.length; i ++){
	        String tag = tagDic.getName(groupTags[i]);
	        createTripleSPO(result, forumPrefix, SNVOC.hasTag, DBP.fullPrefixed(tag));
	        if (!printedTags.containsKey(groupTags[i])) {
	            printedTags.put(groupTags[i], groupTags[i]);
	            createTripleSPO(result, DBP.fullPrefixed(tag), RDF.type, SNVOC.Tag);
	            writeTagData(result, groupTags[i], tag);
	        }
	    }

	    GroupMemberShip memberShips[] = group.getMemberShips();
	    int numMemberAdded = group.getNumMemberAdded();
	    for (int i = 0; i < numMemberAdded; i ++){

	        String memberhipPrefix = SN.getMembershipURI(membershipId);
	        createTripleSPO(result, forumPrefix, SNVOC.hasMember, memberhipPrefix);

	        AddTriple(result, true, false, memberhipPrefix, SNVOC.hasPerson, SN.getPersonURI(memberShips[i].getUserId()));
	        date.setTimeInMillis(memberShips[i].getJoinDate());
	        dateString = DateGenerator.formatDateDetail(date);
	        AddTriple(result, false, true, memberhipPrefix, SNVOC.joinDate,
	                createDataTypeLiteral(dateString, XSD.DateTime));

	        membershipId++;
	    }
        
	    toWriter(result.toString());
	}
	
	private String createLiteral(String value) {
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(value);
		result.append("\"");
		return result.toString();
	}
	
	private String createDataTypeLiteral(String value, String datatypeURI) {
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(value);
		result.append("\"^^");
		result.append(datatypeURI);
		return result.toString();
	}

	/*
	 * Create a triple consisting of subject predicate and object, end with "."
	 */
	private void createTripleSPO(StringBuffer result, String subject, String predicate, String object) {
		result.append(subject);		
		result.append(" ");
		result.append(predicate);
		result.append(" ");
		result.append(object);
		result.append(" .\n");
		
		nrTriples++;
	}
	/*
	 * Create an abbreviated triple consisting of predicate and object; end with ";"
	 */
	private String createTriplePO(String predicate, String object) {
		StringBuffer result = new StringBuffer();
		result.append("    ");
		result.append(predicate);
		result.append(" ");
		result.append(object);
		result.append(" ;\n");
		
		nrTriples++;
		
		return result.toString();
	}

	/*
	 * Create an abbreviated triple consisting of predicate and object; end with "."
	 */
	private String createTriplePOEnd(String predicate, String object) {
		StringBuffer result = new StringBuffer();
		result.append("    ");
		result.append(predicate);
		result.append(" ");
		result.append(object);
		result.append(" .\n");
		
		nrTriples++;
		
		return result.toString();
	}
	
	// Create a triple with two objects separated by an ",". the triple is ended with ";" 
	private String createTriplePOO(String predicate, String object1, String object2) {
		StringBuffer result = new StringBuffer();
		result.append("    ");
		result.append(predicate);
		result.append(" ");
		result.append(object1);
		result.append(" , ");
		result.append(object2);		
		result.append(" ;\n");
		
		nrTriples = nrTriples + 2;
		
		return result.toString();
	}
	
	// Create a triple with two objects separated by an ",". the triple is ended with "." 
	private String createTriplePOOEnd(String predicate, String object1, String object2) {
		StringBuffer result = new StringBuffer();
		result.append("    ");
		result.append(predicate);
		result.append(" ");
		result.append(object1);
		result.append(" , ");
		result.append(object2);		
		result.append(" .\n");
		
		nrTriples = nrTriples + 2;
		
		return result.toString();
	}
	
	public void serialize() {
		try {
			for(int i=0;i<dataFileWriter.length;i++) {
				dataFileWriter[i].flush();
				dataFileWriter[i].close();
				staticdbpFileWriter[i].flush();
                staticdbpFileWriter[i].close();
			}
		} catch(IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
}
