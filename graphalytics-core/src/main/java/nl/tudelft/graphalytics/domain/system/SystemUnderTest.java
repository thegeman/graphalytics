/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.domain.system;

import java.util.*;

/**
 * The system on which a Graphalytics benchmark is executed. This includes information of the number of nodes, the
 * hardware available in those nodes, etc.
 *
 * @author Tim Hegeman
 */
public class SystemUnderTest {

	private final List<Node> nodes;

	public SystemUnderTest(List<Node> nodes) {
		this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public static SystemUnderTest empty() {
		return new SystemUnderTest(Collections.<Node>emptyList());
	}

	public static class Builder {

		private final List<Node> nodes;

		public Builder() {
			this.nodes = new LinkedList<>();
		}

		public Builder withNode(Node node) {
			nodes.add(node);
			return this;
		}

		public Builder withNodes(Node... nodes) {
			for (Node node : nodes) {
				withNode(node);
			}
			return this;
		}

		public Builder withNodes(Collection<Node> nodes) {
			this.nodes.addAll(nodes);
			return this;
		}

		public SystemUnderTest toSystem() {
			return new SystemUnderTest(nodes);
		}

	}

}
