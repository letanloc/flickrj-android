/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.people;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.Response;
import com.aetrion.flickr.Transport;
import com.aetrion.flickr.groups.Group;
import com.aetrion.flickr.photos.Extras;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotoUtils;
import com.aetrion.flickr.util.JSONUtils;
import com.aetrion.flickr.util.StringUtilities;
import com.yuyang226.flickr.org.json.JSONArray;
import com.yuyang226.flickr.org.json.JSONException;
import com.yuyang226.flickr.org.json.JSONObject;

/**
 * Interface for finding Flickr users.
 * 
 * @author Anthony Eden
 * @version $Id: PeopleInterface.java,v 1.28 2010/09/12 20:13:57 x-mago Exp $
 */
public class PeopleInterface {

    public static final String METHOD_FIND_BY_EMAIL = "flickr.people.findByEmail";
    public static final String METHOD_FIND_BY_USERNAME = "flickr.people.findByUsername";
    public static final String METHOD_GET_INFO = "flickr.people.getInfo";
    public static final String METHOD_GET_ONLINE_LIST = "flickr.people.getOnlineList";
    public static final String METHOD_GET_PUBLIC_GROUPS = "flickr.people.getPublicGroups";
    public static final String METHOD_GET_PUBLIC_PHOTOS = "flickr.people.getPublicPhotos";
    public static final String METHOD_GET_UPLOAD_STATUS = "flickr.people.getUploadStatus";
    public static final String METHOD_GET_PHOTOS = "flickr.people.getPhotos";

    private String apiKey;
    private String sharedSecret;
    private Transport transportAPI;

    public PeopleInterface(
        String apiKey,
        String sharedSecret,
        Transport transportAPI
    ) {
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
        this.transportAPI = transportAPI;
    }

    /**
     * Find the user by their email address.
     *
     * This method does not require authentication.
     *
     * @param email The email address
     * @return The User
     * @throws IOException
     * @throws FlickrException
     * @throws JSONException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public User findByEmail(String email) throws IOException, FlickrException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("method", METHOD_FIND_BY_EMAIL));

        parameters.add(new Parameter("find_email", email));

        Response response = transportAPI.postOAuthJSON(apiKey, sharedSecret, parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        return createUser(response.getData());
    }

    /**
     * Find a User by the username.
     *
     * This method does not require authentication.
     *
     * @param username The username
     * @return The User object
     * @throws IOException
     * @throws FlickrException
     * @throws JSONException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public User findByUsername(String username) throws IOException, FlickrException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("method", METHOD_FIND_BY_USERNAME));

        parameters.add(new Parameter("username", username));

        Response response = transportAPI.postOAuthJSON(apiKey, sharedSecret, parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        return createUser(response.getData());
    }
    
    private User createUser(JSONObject rootObject) throws JSONException {
    	JSONObject userElement = rootObject.getJSONObject("user");
        User user = new User();
        user.setId(userElement.getString("nsid"));
        user.setUsername(JSONUtils.getChildValue(userElement, "username"));
        return user;
    }

    /**
     * Get info about the specified user.
     *
     * This method does not require authentication.
     *
     * @param userId The user ID
     * @return The User object
     * @throws IOException
     * @throws FlickrException
     * @throws JSONException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public User getInfo(String userId) throws IOException, FlickrException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("method", METHOD_GET_INFO));
        parameters.add(new Parameter("user_id", userId));

        Response response = transportAPI.postOAuthJSON(apiKey, sharedSecret, parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        JSONObject userElement = response.getData().getJSONObject("person");
        User user = new User();
        user.setId(userElement.getString("nsid"));
        user.setPro("1".equals(userElement.getString("ispro")));
        user.setIconFarm(userElement.getString("iconfarm"));
        user.setIconServer(userElement.getString("iconserver"));
        user.setUsername(JSONUtils.getChildValue(userElement, "username"));
        user.setRealName(JSONUtils.getChildValue(userElement, "realname"));
        user.setLocation(JSONUtils.getChildValue(userElement, "location"));
        user.setPathAlias(userElement.getString("path_alias"));
        user.setMbox_sha1sum(JSONUtils.getChildValue(userElement, "mbox_sha1sum"));
        user.setPhotosurl(JSONUtils.getChildValue(userElement, "photosurl"));
        user.setProfileurl(JSONUtils.getChildValue(userElement, "profileurl"));
        user.setMobileurl(JSONUtils.getChildValue(userElement, "mobileurl"));

        JSONObject photosElement = userElement.getJSONObject("photos");
        user.setPhotosFirstDate(JSONUtils.getChildValue(photosElement, "firstdate"));
        user.setPhotosFirstDateTaken(JSONUtils.getChildValue(photosElement, "firstdatetaken"));
        user.setPhotosCount(JSONUtils.getChildValue(photosElement, "count"));

        return user;
    }

    /**
     * Get a collection of public groups for the user.
     *
     * The groups will contain only the members nsid, name, admin and eighteenplus.
     * If you want the whole group-information, you have to call 
     * {@link com.aetrion.flickr.groups.GroupsInterface#getInfo(String)}.
     *
     * This method does not require authentication.
     *
     * @param userId The user ID
     * @return The public groups
     * @throws IOException
     * @throws FlickrException
     * @throws JSONException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public Collection<Group> getPublicGroups(String userId)
      throws IOException, FlickrException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
        List<Group> groups = new ArrayList<Group>();

        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("method", METHOD_GET_PUBLIC_GROUPS));

        parameters.add(new Parameter("user_id", userId));

        Response response = transportAPI.postOAuthJSON(apiKey, sharedSecret, parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        JSONObject groupsElement = response.getData().getJSONObject("groups");
        JSONArray groupNodes = groupsElement.getJSONArray("group");
        for (int i = 0; i < groupNodes.length(); i++) {
        	JSONObject groupElement = groupNodes.getJSONObject(i);
            Group group = new Group();
            group.setId(groupElement.getString("nsid"));
            group.setName(groupElement.getString("name"));
            group.setAdmin("1".equals(groupElement.getString("admin")));
            group.setEighteenPlus("1".equals(groupElement.getString("eighteenplus")));
            group.setInvitationOnly("1".equals(groupElement.getString("invitation_only")));
            groups.add(group);
        }
        return groups;
    }

    public PhotoList getPublicPhotos(String userId, int perPage, int page)
    throws IOException, FlickrException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
        return getPublicPhotos(userId, Extras.MIN_EXTRAS, perPage, page);
    }

    /**
     * Get a collection of public photos for the specified user ID.
     *
     * This method does not require authentication.
     *
     * @see com.aetrion.flickr.photos.Extras
     * @param userId The User ID
     * @param extras Set of extra-attributes to include (may be null)
     * @param perPage The number of photos per page
     * @param page The page offset
     * @return The PhotoList collection
     * @throws IOException
     * @throws FlickrException
     * @throws JSONException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public PhotoList getPublicPhotos(String userId, Set<String> extras, int perPage, int page) 
    throws IOException, FlickrException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
        PhotoList photos = new PhotoList();

        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("method", METHOD_GET_PUBLIC_PHOTOS));

        parameters.add(new Parameter("user_id", userId));

        if (perPage > 0) {
            parameters.add(new Parameter("per_page", "" + perPage));
        }
        if (page > 0) {
            parameters.add(new Parameter("page", "" + page));
        }

        if (extras != null) {
            parameters.add(new Parameter(Extras.KEY_EXTRAS, StringUtilities.join(extras, ",")));
        }

        Response response = transportAPI.postOAuthJSON(apiKey, sharedSecret, parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        JSONObject photosElement = response.getData().getJSONObject("photos");
        photos.setPage(photosElement.getInt("page"));
		photos.setPages(photosElement.getInt("pages"));
		photos.setPerPage(photosElement.getInt("perpage"));
		photos.setTotal(photosElement.getInt("total"));

        JSONArray photoNodes = photosElement.getJSONArray("photo");
        for (int i = 0; i < photoNodes.length(); i++) {
            JSONObject photoElement = photoNodes.getJSONObject(i);
            photos.add(PhotoUtils.createPhoto(photoElement));
        }
        return photos;
    }

    /**
     * Get upload status for the currently authenticated user.
     *
     * Requires authentication with 'read' permission using the new authentication API.
     *
     * @return A User object with upload status data fields filled
     * @throws IOException
     * @throws FlickrException
     * @throws JSONException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public User getUploadStatus() throws IOException, FlickrException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("method", METHOD_GET_UPLOAD_STATUS));

        Response response = transportAPI.postOAuthJSON(apiKey, sharedSecret, parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        JSONObject userElement = response.getData().getJSONObject("user");
        User user = new User();
        user.setId(userElement.getString("id"));
        user.setPro("1".equals(userElement.getString("ispro")));
        user.setUsername(JSONUtils.getChildValue(userElement, "username"));

        JSONObject bandwidthElement = userElement.getJSONObject("bandwidth");
        Bandwidth bandwidth = new Bandwidth();
        bandwidth.setMax(bandwidthElement.getLong("max"));
        bandwidth.setUsed(bandwidthElement.getLong("used"));
        bandwidth.setUsed(bandwidthElement.getLong("used"));
        bandwidth.setMaxBytes(bandwidthElement.getLong("maxbytes"));
        bandwidth.setUsedBytes(bandwidthElement.getLong("usedbytes"));
        bandwidth.setRemainingBytes(bandwidthElement.getLong("remainingbytes"));
        bandwidth.setMaxKb(bandwidthElement.getLong("maxkb"));
        bandwidth.setUsedKb(bandwidthElement.getLong("usedkb"));
        bandwidth.setRemainingKb(bandwidthElement.getLong("remainingkb"));
        bandwidth.setUnlimited("1".equals(bandwidthElement.getString("unlimited")));
        user.setBandwidth(bandwidth);
        
        JSONObject filesizeElement = userElement.getJSONObject("filesize");
        user.setFilesizeMax(filesizeElement.getString("max"));

        return user;
    }

	/**
	 * Returns photos from the given user's photostream. Only photos visible the
	 * calling user will be returned. this method must be authenticated.
	 * 
	 * @param userId
	 * @param extras
	 * @param perpage
	 * @param page
	 * @return
	 * @throws IOException
	 * @throws FlickrException
	 * @throws JSONException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public PhotoList getPhotos(String userId, Set<String> extras, int perPage,
			int page) throws IOException, FlickrException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
		PhotoList photos = new PhotoList();

		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("method", METHOD_GET_PHOTOS));
		parameters.add(new Parameter("user_id", userId));

		if (perPage > 0) {
			parameters.add(new Parameter("per_page", "" + perPage));
		}
		if (page > 0) {
			parameters.add(new Parameter("page", "" + page));
		}

		if (extras != null) {
			parameters.add(new Parameter(Extras.KEY_EXTRAS, StringUtilities
					.join(extras, ",")));
		}

		Response response = transportAPI
				.postOAuthJSON(apiKey, sharedSecret, parameters);
		if (response.isError()) {
			throw new FlickrException(response.getErrorCode(), response
					.getErrorMessage());
		}
		JSONObject photosElement = response.getData().getJSONObject("photos");
		photos.setPage(photosElement.getInt("page"));
		photos.setPages(photosElement.getInt("pages"));
		photos.setPerPage(photosElement.getInt("perpage"));
		photos.setTotal(photosElement.getInt("total"));

		JSONArray photoNodes = photosElement.getJSONArray("photo");
		for (int i = 0; i < photoNodes.length(); i++) {
			JSONObject photoElement = photoNodes.getJSONObject(i);
			photos.add(PhotoUtils.createPhoto(photoElement));
		}
		return photos;
	}
}
